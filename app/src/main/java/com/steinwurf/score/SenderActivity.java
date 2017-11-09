package com.steinwurf.score;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.steinwurf.score.sender.Sender;

public class SenderActivity extends AppCompatActivity {

    private static final String TAG = SenderActivity.class.getSimpleName();
    private static final String SENDER_CONFIGURATION = "SENDER_CONFIGURATION";

    private static final int UI_UPDATE_RATE = 500;

    // network
    private static final String SENDER_PORT = "SENDER_PORT";
    private static final String SENDER_IP = "SENDER_IP";
    private static final String SENDER_SPEED = "SENDER_SPEED";
    private static final String SENDER_MESSAGE_SIZE = "SENDER_MESSAGE_SIZE";

    // protocol
    private static final String SYMBOL_SIZE = "SYMBOL_SIZE";
    private static final String GENERATION_SIZE = "GENERATION_SIZE";
    private static final String GENERATION_WINDOW_SIZE = "GENERATION_WINDOW_SIZE";
    private static final String DATA_REDUNDANCY = "DATA_REDUNDANCY";
    private static final String FEEDBACK_PROBABILITY = "FEEDBACK_PROBABILITY";

    enum State
    {
        connected,
        disconnected,
        intermediate
    }

    private final ScoreEncoder encoder = new ScoreEncoder();
    private final Server server = new Server(encoder);


    private final View.OnClickListener connectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeState(State.intermediate);
            String ipString = ipEditText.getText().toString();
            String portString = portEditText.getText().toString();
            server.start(ipString, portString);
        }
    };

    private final View.OnClickListener disconnectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeState(State.intermediate);
            server.stop();
        }
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateUI = new Runnable() {

        private final int uid = android.os.Process.myUid();

        private final double intervalSeconds = UI_UPDATE_RATE * 0.001;

        private double previousReceived;
        private double previousSent;

        @Override
        public void run() {

            double totalBytesReceived = TrafficStats.getUidRxBytes(uid);
            double totalBytesSent = TrafficStats.getUidTxBytes(uid);

            if (totalBytesReceived == TrafficStats.UNSUPPORTED || totalBytesSent == TrafficStats.UNSUPPORTED) {
                Log.w(TAG, "The use of TrafficStats is not supported on this device.");
                return;
            }

            if (previousReceived >= 0 && previousSent >= 0) {

                double received = (totalBytesReceived - previousReceived) / intervalSeconds;
                double sent = (totalBytesSent - previousSent) / intervalSeconds;
                String format = "Received: %8s/s Sent: %12s/s";
                networkStatsTextView.setText(String.format(format, Utils.bytesToPrettyString(received), Utils.bytesToPrettyString(sent)));
            }
            previousReceived = totalBytesReceived;
            previousSent = totalBytesSent;

            handler.postDelayed(this, UI_UPDATE_RATE);
        }
    };

    WifiManager.MulticastLock multicastLock;

    private Button connectButton;

    private LinearLayout configurationLinearLayout;
    private EditText ipEditText;
    private EditText portEditText;

    private LinearLayout controlLinearLayout;

    TextView networkStatsTextView;

    // Network
    private SeekBarHelper generatorSpeedSeekBar;
    private SeekBarHelper messageSizeSeekBar;
    // Protocol
    private SeekBarHelper symbolSizeSeekBar;
    private SeekBarHelper generationSizeSeekBar;
    private SeekBarHelper generationWindowSizeSeekBar;
    private SeekBarHelper dataRedundancySeekBar;
    private SeekBarHelper feedbackProbabilitySeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
        setTitle(getString(R.string.sender));

        connectButton = findViewById(R.id.connectButton);

        configurationLinearLayout = findViewById(R.id.configurationLinearLayout);
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);

        controlLinearLayout = findViewById(R.id.controlLinearLayout);

        networkStatsTextView = findViewById(R.id.networkStatsTextView);

        setUpSeekBars();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences(SENDER_CONFIGURATION, MODE_PRIVATE);
        ipEditText.setText(preferences.getString(SENDER_IP, "224.0.0.251"));
        portEditText.setText(preferences.getString(SENDER_PORT, "9010"));

        generatorSpeedSeekBar.setProgress(preferences.getInt(SENDER_SPEED, generatorSpeedSeekBar.valueToProgress(1000)));
        messageSizeSeekBar.setProgress(preferences.getInt(SENDER_MESSAGE_SIZE, messageSizeSeekBar.valueToProgress(1000)));
        symbolSizeSeekBar.setProgress(preferences.getInt(SYMBOL_SIZE, symbolSizeSeekBar.valueToProgress(1000)));
        generationSizeSeekBar.setProgress(preferences.getInt(GENERATION_SIZE, generationSizeSeekBar.valueToProgress(64)));
        generationWindowSizeSeekBar.setProgress(preferences.getInt(GENERATION_WINDOW_SIZE, generationWindowSizeSeekBar.valueToProgress(20)));
        dataRedundancySeekBar.setProgress(preferences.getInt(DATA_REDUNDANCY, dataRedundancySeekBar.valueToProgress(10)));
        feedbackProbabilitySeekBar.setProgress(preferences.getInt(FEEDBACK_PROBABILITY, feedbackProbabilitySeekBar.valueToProgress(50)));


        server.setServerHandler(new Server.IServerHandler() {

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
                        Toast.makeText(SenderActivity.this, reason, Toast.LENGTH_LONG).show();
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

        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wm != null;
        multicastLock = wm.createMulticastLock(TAG);
        multicastLock.acquire();
        handler.post(updateUI);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(updateUI);
        server.setServerHandler(null);
        server.stop();

        getSharedPreferences(SENDER_CONFIGURATION, MODE_PRIVATE)
                .edit()
                .putString(SENDER_PORT, portEditText.getText().toString())
                .putString(SENDER_IP, ipEditText.getText().toString())
                .putInt(SENDER_SPEED, generatorSpeedSeekBar.getProgress())
                .putInt(SENDER_MESSAGE_SIZE, messageSizeSeekBar.getProgress())
                .putInt(SYMBOL_SIZE, symbolSizeSeekBar.getProgress())
                .putInt(GENERATION_SIZE, generationSizeSeekBar.getProgress())
                .putInt(GENERATION_WINDOW_SIZE, generationWindowSizeSeekBar.getProgress())
                .putInt(DATA_REDUNDANCY, dataRedundancySeekBar.getProgress())
                .putInt(FEEDBACK_PROBABILITY, feedbackProbabilitySeekBar.getProgress())
                .apply();


        multicastLock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void changeState(State newState)
    {
        switch (newState)
        {
            case connected:
                connectButton.setText(R.string.disconnect);
                connectButton.setOnClickListener(disconnectOnClickListener);
                connectButton.setEnabled(true);
                configurationLinearLayout.setVisibility(View.GONE);
                controlLinearLayout.setVisibility(View.VISIBLE);
                break;
            case intermediate:
                connectButton.setOnClickListener(null);
                connectButton.setEnabled(false);
                configurationLinearLayout.setVisibility(View.GONE);
                controlLinearLayout.setVisibility(View.GONE);
                break;
            case disconnected:
                connectButton.setText(R.string.connect);
                connectButton.setOnClickListener(connectOnClickListener);
                connectButton.setEnabled(true);
                configurationLinearLayout.setVisibility(View.VISIBLE);
                controlLinearLayout.setVisibility(View.GONE);
                break;
        }
    }


    private void setUpSeekBars() {

        generatorSpeedSeekBar = new SeekBarHelper((SeekBar)findViewById(R.id.speedSeekBar),
                (TextView)findViewById(R.id.speedTextView),
                false) {
            @Override
            public void setText(double value) {
                if ((int)value == ScoreEncoder.MAX_SPEED)
                {
                    textView.setText(R.string.max);
                    return;
                }
                String format = "%12s/s";
                textView.setText(String.format(format, Utils.bytesToPrettyString(value)));
            }
        };

        generatorSpeedSeekBar.setMax(ScoreEncoder.MAX_SPEED);
        generatorSpeedSeekBar.setMin(1);

        generatorSpeedSeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {
            @Override
            public void onProgressChanged(double speed) {
                if ((int)speed == ScoreEncoder.MAX_SPEED)
                {
                    encoder.enableRateLimiter(false);
                    return;
                }
                encoder.enableRateLimiter(true);
                encoder.setGeneratorSpeed((int)speed);
            }
        });

        messageSizeSeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.messageSizeSeekBar),
                (TextView)findViewById(R.id.messageSizeTextView),
                false);
        messageSizeSeekBar.setMax(ScoreEncoder.MAX_MESSAGE_SIZE);
        messageSizeSeekBar.setMin(ScoreEncoder.MIN_MESSAGE_SIZE);

        messageSizeSeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {

            @Override
            public void onProgressChanged(double messageSize) {
                encoder.setMessageSize((int)messageSize);
            }
        });

        symbolSizeSeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.symbolSizeSeekBar),
                (TextView)findViewById(R.id.symbolSizeTextView),
                false);
        symbolSizeSeekBar.setMax(Sender.MAX_SYMBOL_SIZE);
        symbolSizeSeekBar.setMin(12);

        symbolSizeSeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {

            @Override
            public void onProgressChanged(double symbolSize) {
                encoder.setSymbolSize((int)symbolSize);
            }
        });

        generationSizeSeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.generationSizeSeekBar),
                (TextView)findViewById(R.id.generationSizeTextView),
                false);
        generationSizeSeekBar.setMax(Sender.MAX_GENERATION_SIZE);
        generationSizeSeekBar.setMin(1);

        generationSizeSeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {
            @Override
            public void onProgressChanged(double generationSize) {
                encoder.setGenerationSize((int)generationSize);
            }
        });

        generationWindowSizeSeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.generationWindowSizeSeekBar),
                (TextView)findViewById(R.id.generationWindowSizeTextView),
                false);
        generationWindowSizeSeekBar.setMax(Sender.MAX_GENERATION_SIZE);

        generationWindowSizeSeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {
            @Override
            public void onProgressChanged(double generationWindowSize) {
                encoder.setGenerationWindowSize((int)generationWindowSize);
            }
        });

        dataRedundancySeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.dataRedundancySeekBar),
                (TextView)findViewById(R.id.dataRedundancyTextView),
                true);
        dataRedundancySeekBar.setMax(200);
        dataRedundancySeekBar.setMin(0);

        dataRedundancySeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {
            @Override
            public void onProgressChanged(double dataRedundancy) {
                encoder.setDataRedundancy((float)dataRedundancy / 100);
            }
        });

        feedbackProbabilitySeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.feedbackProbabilitySeekBar),
                (TextView)findViewById(R.id.feedbackProbabilityTextView),
                true);
        feedbackProbabilitySeekBar.setMax(100);
        feedbackProbabilitySeekBar.setMin(0);

        feedbackProbabilitySeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {
            @Override
            public void onProgressChanged(double feedbackProbability) {
                encoder.setFeedbackProbability((float)feedbackProbability / 100);
            }
        });
    }
}
