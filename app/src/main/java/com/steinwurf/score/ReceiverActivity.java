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
import java.net.UnknownHostException;
import java.util.Locale;


public class ReceiverActivity extends AppCompatActivity {

    private static final String TAG = ReceiverActivity.class.getSimpleName();
    private static final String RECEIVER_CONFIGURATION = "RECEIVER_CONFIGURATION";
    private static final String RECEIVER_PORT = "RECEIVER_PORT";
    private static final String RECEIVER_IP = "RECEIVER_IP";
    private static final String RECEIVER_KEEPALIVE_INTERVAL = "RECEIVER_KEEPALIVE_INTERVAL";

    private static final int UI_UPDATE_RATE = 100;
    private static final int MAX_KEEPALIVE_INTERVAL = 500;
    private static final int MAX_DATAPOINTS = 100;
    private final Handler handler = new Handler();

    private final ScoreDecoder decoder = new ScoreDecoder();
    private final Client client = new Client(decoder);

    private KeepAlive mKeepAlive;

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
            client.start(ipString, portString);
        }
    };

    private View.OnClickListener disconnectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeState(State.intermediate);
            client.stop();
        }
    };

    // stats
    private LineGraphSeries<DataPoint> goodPutSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> messageLossSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> packetLossSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> delaySeries = new LineGraphSeries<>();

    private Runnable updateStats = new Runnable() {
        @Override
        public void run() {
            float packetLossPercentage = decoder.packetLossPercentage();
            float messageLossPercentage = decoder.messageLossPercentage();
            float goodput = decoder.getGoodPut();
            long delay = decoder.getDelay();

            addToGraph(packetLossSeries, packetLossPercentage);
            addToGraph(messageLossSeries, messageLossPercentage);
            addToGraph(goodPutSeries, goodput);
            addToGraph(delaySeries, delay);

            String format = (
                    "Count : %d pkt / %d msg\n" +
                    "Loss  : %d pkt / %d msg - %.2f / %.2f %%\n" +
                    "Bytes : %d pkt / %d msg - goodput: %.2f %%\n" +
                    "Delay : %d ms / %d msg");

            statusTextView.setText(String.format(Locale.getDefault(),
                    format,
                    decoder.getPacketsReceived(), decoder.getMessagesReceived(),
                    decoder.getPacketsLost(), decoder.getMessagesLost(), packetLossPercentage, messageLossPercentage,
                    decoder.getPacketBytesReceived(), decoder.getMessageBytesReceived(), goodput,
                    delay, decoder.getMessagesBehind()));
        }
    };

    private Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(updateStats);
            handler.postDelayed(this, UI_UPDATE_RATE);
        }
    };


    private void addToGraph(LineGraphSeries<DataPoint> series, float yValue) {
        series.appendData(new DataPoint(series.getHighestValueX() + 1, yValue), true, MAX_DATAPOINTS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        setTitle(getString(R.string.receiver));

        SharedPreferences preferences = getSharedPreferences(RECEIVER_CONFIGURATION, MODE_PRIVATE);
        int keepAliveInterval = preferences.getInt(RECEIVER_KEEPALIVE_INTERVAL, 50);

        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wm != null;

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
        packetLossSeries.setTitle("packet loss");
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
        graphView.getSecondScale().addSeries(delaySeries);
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.setClientHandler(new Client.IClientHandler() {
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

        changeState(State.disconnected);
        handler.post(updateUI);
    }

    @Override
    protected void onStop() {
        super.onStop();

        handler.removeCallbacks(updateUI);
        client.setClientHandler(null);
        client.stop();
        mKeepAlive.stop();
        resetStats();
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

    private void resetStats()
    {
        decoder.resetStats();
        packetLossSeries.resetData(new DataPoint[]{});
        messageLossSeries.resetData(new DataPoint[]{});
        goodPutSeries.resetData(new DataPoint[]{});
        delaySeries.resetData(new DataPoint[]{});
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
}
