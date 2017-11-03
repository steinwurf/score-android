package com.steinwurf.score;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;


public class ReceiverActivity extends AppCompatActivity implements ScoreDecoder.IOnMessageHandler {

    private static final String TAG = ReceiverActivity.class.getSimpleName();
    private static final String RECEIVER_CONFIGURATION = "RECEIVER_CONFIGURATION";
    private static final String RECEIVER_PORT = "RECEIVER_PORT";
    private static final String RECEIVER_IP = "RECEIVER_IP";
    private static final String RECEIVER_KEEPALIVE_INTERVAL = "RECEIVER_KEEPALIVE_INTERVAL";

    private static final int MAX_KEEPALIVE_INTERVAL = 500;
    private static final int MAX_DATAPOINTS = 100;
    private final Handler handler = new Handler();

    private Client mClient;
    private KeepAlive mKeepAlive;
    private ScoreDecoder mScoreDecoder;

    enum State
    {
        connected,
        disconnected,
        intermediate
    }

    private Button connectButton;
    private LinearLayout configurationLinearLayout;
    private EditText ipEditText;
    private EditText portEditText;

    private LinearLayout statusLinearLayout;
    ToggleButton keepAliveToggleButton;

    private SeekBarHelper keepAliveIntervalSeekBar;

    private TextView statusTextView;

    WifiManager.MulticastLock multicastLock;

    private View.OnClickListener connectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeState(State.intermediate);
            String ipString = ipEditText.getText().toString();
            String portString = portEditText.getText().toString();
            mClient.start(ipString, portString);
        }
    };

    private View.OnClickListener disconnectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeState(State.intermediate);
            mClient.stop();
        }
    };

    // stats
    private LineGraphSeries<DataPoint> goodPutSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> messageLossSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> packetLossSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> delaySeries = new LineGraphSeries<>();

    private long messageCount = 0;
    private long messageBytesReceived = 0;
    private long messageLoss = 0;
    private Long lastMessageId = null;
    private Long lastMessageTimestamp = null;
    private Long lastMessageSize = null;

    private long packetCount = 0;
    private long packetBytesReceived = 0;
    private long packetLoss = 0;
    private Long lastPacketId = null;
    private Long lastPacketTimestamp = null;
    private Long lastPacketSize = null;

    private Long currentMessageId = null;

    private long UI_UPDATE_RATE = 100;
    private Runnable updateStats = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    float packetLossPercentage = packetCount == 0 ? 0 : ((float)packetLoss / (float)packetCount) * 100;
                    float messageLossPercentage = messageCount == 0 ? 0 : ((float)messageLoss / (float)messageCount) * 100;
                    float goodput = ((float)messageBytesReceived / (float)packetBytesReceived) * 100;
                    packetLossSeries.appendData(new DataPoint(packetLossSeries.getHighestValueX() + 1, packetLossPercentage), true, MAX_DATAPOINTS);
                    messageLossSeries.appendData(new DataPoint(messageLossSeries.getHighestValueX() + 1, messageLossPercentage), true, MAX_DATAPOINTS);
                    goodPutSeries.appendData(new DataPoint(goodPutSeries.getHighestValueX() + 1, goodput), true, MAX_DATAPOINTS);

                    long delay = 0;
                    if (lastMessageTimestamp != null)
                        delay = lastPacketTimestamp - lastMessageTimestamp;
                    delaySeries.appendData(new DataPoint(delaySeries.getHighestValueX() + 1, delay), true, MAX_DATAPOINTS);
                    long messagesBehind = 0;
                    if (currentMessageId != null && lastMessageId != null)
                    {
                        messagesBehind = currentMessageId - lastMessageId;
                    }

                    String format = (
                            "Count : %d pkt / %d msg\n" +
                            "Loss  : %d pkt / %d msg - %.2f / %.2f %%\n" +
                            "Bytes : %d pkt / %d msg - goodput: %.2f %%\n" +
                            "Delay : %d ms / %d msg");
                    statusTextView.setText(String.format(Locale.getDefault(),
                            format,
                            packetCount, messageCount,
                            packetLoss, messageLoss, packetLossPercentage, messageLossPercentage,
                            packetBytesReceived, messageBytesReceived, goodput,
                            delay,
                            messagesBehind));
                }
            });
            handler.postDelayed(this, UI_UPDATE_RATE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        setTitle(getString(R.string.receiver));

        SharedPreferences preferences = getSharedPreferences(RECEIVER_CONFIGURATION, MODE_PRIVATE);
        int keepAliveInterval = preferences.getInt(RECEIVER_KEEPALIVE_INTERVAL, 50);

        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wm != null;

        mClient = new Client();
        mScoreDecoder = new ScoreDecoder(this);
        mKeepAlive = createKeepAlive(wm, keepAliveInterval);

        connectButton = findViewById(R.id.connectButton);

        configurationLinearLayout = findViewById(R.id.configurationLinearLayout);
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);

        statusLinearLayout = findViewById(R.id.statusLinearLayout);
        keepAliveToggleButton = findViewById(R.id.keepAliveToggleButton);
        keepAliveIntervalSeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.keepAliveIntervalSeekBar),
                (TextView)findViewById(R.id.keepAliveIntervalTextView),
                false);
        keepAliveIntervalSeekBar.setMax(MAX_KEEPALIVE_INTERVAL);

        statusTextView = findViewById(R.id.statusTextView);

        setupGraphView();

        keepAliveIntervalSeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {
            @Override
            public void onProgressChanged(double value) {
                mKeepAlive.setInterval((int)value);
            }
        });


        keepAliveToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    mKeepAlive.start();
                } else {
                    mKeepAlive.stop();
                }
            }
        });

        findViewById(R.id.resetButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetStats();
            }
        });

        ipEditText.setText(preferences.getString(RECEIVER_IP, "224.0.0.251"));
        portEditText.setText(preferences.getString(RECEIVER_PORT, "9010"));
        keepAliveIntervalSeekBar.setProgress(keepAliveInterval);

        multicastLock = wm.createMulticastLock(TAG);
        multicastLock.acquire();
    }

    private KeepAlive createKeepAlive(WifiManager wm, int keepAliveInterval) {
        try
        {
            String gatewayIP = "192.168.0.1";
            DhcpInfo dhcp = wm.getDhcpInfo();
            if (dhcp != null)
            {
                //noinspection deprecation
                gatewayIP = Formatter.formatIpAddress(dhcp.gateway);
            }
            return new KeepAlive(InetAddress.getByName(gatewayIP), 13337, keepAliveInterval);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void setupGraphView() {
        GraphView graphView = findViewById(R.id.lossGraphView);
        int colorPrimaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
        graphView.getGridLabelRenderer().setHorizontalLabelsColor(colorPrimaryDark);
        graphView.getGridLabelRenderer().setVerticalLabelsColor(colorPrimaryDark);

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(100);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(MAX_DATAPOINTS);

        packetLossSeries.setThickness(5);
        packetLossSeries.setColor(Color.RED);
        graphView.addSeries(packetLossSeries);

        messageLossSeries.setThickness(5);
        messageLossSeries.setColor(Color.YELLOW);
        graphView.addSeries(messageLossSeries);

        goodPutSeries.setThickness(5);
        goodPutSeries.setColor(colorPrimary);
        graphView.addSeries(goodPutSeries);

        delaySeries.setThickness(5);
        delaySeries.setColor(Color.BLUE);
        graphView.getSecondScale().setMaxY(5000);
        graphView.getSecondScale().setMinY(0);
        graphView.addSeries(delaySeries);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.setClientHandler(new Client.IClientHandler() {
            @Override
            public void onStarted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeState(State.connected);
                    }
                });
            }

            @Override
            public void onError(final String reason) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ReceiverActivity.this, reason, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onStopped() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeState(State.disconnected);
                    }
                });
            }
        });

        mClient.setOnDataHandler(new Client.IOnDataHandler() {
            @Override
            public void onData(SocketAddress senderAddress, final ByteBuffer buffer) {

                buffer.order(ByteOrder.BIG_ENDIAN);
                long id = buffer.getInt() & 0x00000000ffffffffL;
                long timestamp = buffer.getLong();
                currentMessageId = buffer.getInt() & 0x00000000ffffffffL;

                lastPacketSize = (long)buffer.limit();
                if (lastPacketTimestamp != null && lastPacketId != null)
                {
                    if (lastPacketTimestamp > timestamp)
                    {
                        Log.w(TAG, "OutOfOrder packet received");
                    }
                    else
                    {
                        packetLoss += calculateLoss(lastPacketId, id);
                    }
                }
                lastPacketTimestamp = timestamp;
                lastPacketId = id;
                packetCount += 1;
                packetBytesReceived += buffer.limit();
                mScoreDecoder.onData(senderAddress, buffer);
            }
        });

        changeState(State.disconnected);
        handler.post(updateStats);
    }

    @Override
    protected void onStop() {
        super.onStop();

        handler.removeCallbacks(updateStats);
        mClient.setClientHandler(null);
        mClient.setOnDataHandler(null);
        mClient.stop();
        mKeepAlive.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSharedPreferences(RECEIVER_CONFIGURATION, MODE_PRIVATE)
                .edit()
                .putString(RECEIVER_PORT, portEditText.getText().toString())
                .putString(RECEIVER_IP, ipEditText.getText().toString())
                .putInt(RECEIVER_KEEPALIVE_INTERVAL, keepAliveIntervalSeekBar.getProgress())
                .apply();

        multicastLock.release();
    }

    @Override
    public void onMessage(ByteBuffer message) {

        message.order(ByteOrder.BIG_ENDIAN);
        long id = message.getInt() & 0x00000000ffffffffL;
        long timestamp = message.getLong();

        lastMessageSize = (long)message.limit();

        if (lastMessageTimestamp != null && lastMessageId != null)
        {
            messageLoss += calculateLoss(lastMessageId, id);
        }
        lastMessageTimestamp = timestamp;
        lastMessageId = id;
        messageCount += 1;
        messageBytesReceived += message.limit();
    }

    private void resetStats()
    {
        messageCount = 0;
        messageBytesReceived = 0;
        messageLoss = 0;
        lastMessageId = null;
        lastMessageTimestamp = null;
        lastMessageSize = null;

        packetCount = 0;
        packetBytesReceived = 0;
        packetLoss = 0;
        lastPacketId = null;
        lastPacketTimestamp = null;
        lastPacketSize = null;
        packetLossSeries.resetData(new DataPoint[]{});
        messageLossSeries.resetData(new DataPoint[]{});
        goodPutSeries.resetData(new DataPoint[]{});
        delaySeries.resetData(new DataPoint[]{});

        currentMessageId = null;
    }

    private void changeState(State newState)
    {
        resetStats();
        switch (newState)
        {
            case connected:
                connectButton.setText(R.string.disconnect);
                connectButton.setOnClickListener(disconnectOnClickListener);
                connectButton.setEnabled(true);
                configurationLinearLayout.setVisibility(View.GONE);
                statusLinearLayout.setVisibility(View.VISIBLE);
                break;
            case intermediate:
                connectButton.setOnClickListener(null);
                connectButton.setEnabled(false);
                configurationLinearLayout.setVisibility(View.GONE);
                statusLinearLayout.setVisibility(View.GONE);
                break;
            case disconnected:
                connectButton.setText(R.string.connect);
                connectButton.setOnClickListener(connectOnClickListener);
                connectButton.setEnabled(true);
                configurationLinearLayout.setVisibility(View.VISIBLE);
                statusLinearLayout.setVisibility(View.GONE);
                break;
        }
    }

    private static long calculateLoss(long id, long newId)
    {
        if (newId <= id)
            newId += 4294967296L;
        return (newId - id) - 1;
    }
}
