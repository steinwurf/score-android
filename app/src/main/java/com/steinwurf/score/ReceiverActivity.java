package com.steinwurf.score;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class ReceiverActivity extends AppCompatActivity {

    private static final String TAG = ReceiverActivity.class.getSimpleName();
    private static final String RECEIVER_CONFIGURATION = "RECEIVER_CONFIGURATION";
    private static final String RECEIVER_PORT = "RECEIVER_PORT";
    private static final String RECEIVER_IP = "RECEIVER_IP";
    private static final String RECEIVER_KEEPALIVE_INTERVAL = "RECEIVER_KEEPALIVE_INTERVAL";

    private static final int MAX_KEEPALIVE_INTERVAL = 500;
    private static final int MAX_DATAPOINTS = 100;
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
    TextView keepAliveIntervalTextView;
    SeekBar keepAliveIntervalSeekBar;
    private TextView statusTextView;

    private LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

    WifiManager.MulticastLock multicastLock;

    private Client mClient;

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

    long dataLoss = 0;
    long dataOutOfOrder = 0;
    Long lastId = null;
    Long lastTimestamp = null;

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
        mKeepAlive = createKeepAlive(wm, keepAliveInterval);

        connectButton = findViewById(R.id.connectButton);

        configurationLinearLayout = findViewById(R.id.configurationLinearLayout);
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);

        statusLinearLayout = findViewById(R.id.statusLinearLayout);
        keepAliveToggleButton = findViewById(R.id.keepAliveToggleButton);
        keepAliveIntervalTextView = findViewById(R.id.keepAliveIntervalTextView);
        keepAliveIntervalSeekBar = findViewById(R.id.keepAliveIntervalSeekBar);
        statusTextView = findViewById(R.id.statusTextView);

        setupGrapView();

        keepAliveIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float percentage = i / (float)seekBar.getMax();
                int interval = i == 0 ? 1 : (int)(percentage * MAX_KEEPALIVE_INTERVAL);

                keepAliveIntervalTextView.setText(Integer.toString(interval));
                mKeepAlive.setInterval(interval);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

        ipEditText.setText(preferences.getString(RECEIVER_IP, "224.0.0.251"));
        portEditText.setText(preferences.getString(RECEIVER_PORT, "9010"));
        keepAliveIntervalSeekBar.setProgress(1);
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

    private void setupGrapView() {
        GraphView lossGraphView = findViewById(R.id.lossGraphView);
        int colorPrimaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
        lossGraphView.getGridLabelRenderer().setHorizontalLabelsColor(colorPrimaryDark);
        lossGraphView.getGridLabelRenderer().setVerticalLabelsColor(colorPrimaryDark);

        lossGraphView.getViewport().setYAxisBoundsManual(true);
        lossGraphView.getViewport().setMinY(0);
        lossGraphView.getViewport().setMaxY(2050);
        lossGraphView.getViewport().setXAxisBoundsManual(true);
        lossGraphView.getViewport().setMinX(0);
        lossGraphView.getViewport().setMaxX(MAX_DATAPOINTS);

        series.setThickness(5);
        series.setColor(colorPrimary);
        lossGraphView.addSeries(series);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.setHandler(new Client.IClientHandler() {
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

            @Override
            public void onData(final ByteBuffer buffer) {

                buffer.order(ByteOrder.BIG_ENDIAN);
                long id = buffer.getInt() & 0x00000000ffffffffL;
                long timestamp = buffer.getLong() & 0x00000000ffffffffL;

                if (lastTimestamp != null && lastId != null)
                {
                    if (lastTimestamp > timestamp)
                    {
                        dataOutOfOrder += 1;
                        dataLoss -= 1;
                    }
                    else
                    {
                        dataLoss += calculateLoss(lastId, id);
                    }
                }
                lastTimestamp = timestamp;
                lastId = id;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double x = series.getHighestValueX() + 1;
                        series.appendData(new DataPoint(x, buffer.remaining()), true, MAX_DATAPOINTS);
                        statusTextView.setText(String.format("Data loss: %d\nData Out of Order: %d", dataLoss, dataOutOfOrder));
                    }
                });
            }
        });
        changeState(State.disconnected);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mClient.setHandler(null);
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

    void changeState(State newState)
    {
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
