package com.steinwurf.score;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.steinwurf.score.sender.Sender;

import java.io.IOException;

public class SenderActivity extends AppCompatActivity implements ScoreEncoder.IOnMessageHandler {

    private static final String TAG = SenderActivity.class.getSimpleName();
    private static final String SENDER_CONFIGURATION = "SENDER_CONFIGURATION";

    // network
    private static final String SENDER_PORT = "SENDER_PORT";
    private static final String SENDER_IP = "SENDER_IP";
    private static final String SENDER_SPEED = "SENDER_SPEED";
    private static final String SENDER_BUFFER_SIZE = "SENDER_BUFFER_SIZE";

    // protocol
    private static final String SYMBOL_SIZE = "SENDER_BUFFER_SIZE";
    private static final String GENERATION_SIZE = "SENDER_BUFFER_SIZE";
    private static final String GENERATION_WINDOW_SIZE = "SENDER_BUFFER_SIZE";
    private static final String DATA_REDUNDANCY = "SENDER_BUFFER_SIZE";
    private static final String FEEDBACK_PROBABILITY = "SENDER_BUFFER_SIZE";

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

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

    private LinearLayout controlLinearLayout;

    // Network
    private SeekBarHelper speedSeekBar;
    private SeekBarHelper bufferSizeSeekBar;
    // Protocol
    private SeekBarHelper symbolSizeSeekBar;
    private SeekBarHelper generationSizeSeekBar;
    private SeekBarHelper generationWindowSizeSeekBar;
    private SeekBarHelper dataRedundancySeekBar;
    private SeekBarHelper feedbackProbabilitySeekBar;

    WifiManager.MulticastLock multicastLock;

    private Server mServer;
    private MessageGenerator mMessageGenerator;
    private ScoreEncoder mScoreEncoder;

    private View.OnClickListener connectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeState(State.intermediate);
            String ipString = ipEditText.getText().toString();
            String portString = portEditText.getText().toString();
            mServer.start(ipString, portString);
        }
    };

    private View.OnClickListener disconnectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeState(State.intermediate);
            mServer.stop();
        }
    };

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

        mServer = new Server();
        mScoreEncoder = new ScoreEncoder(this);
        mMessageGenerator = new MessageGenerator(mScoreEncoder);

        setUpSeekBars();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences(SENDER_CONFIGURATION, MODE_PRIVATE);
        ipEditText.setText(preferences.getString(SENDER_IP, "224.0.0.251"));
        portEditText.setText(preferences.getString(SENDER_PORT, "9010"));

        speedSeekBar.setProgress(preferences.getInt(SENDER_SPEED, speedSeekBar.valueToProgress(1000)));
        bufferSizeSeekBar.setProgress(preferences.getInt(SENDER_BUFFER_SIZE, bufferSizeSeekBar.valueToProgress(1000)));
        symbolSizeSeekBar.setProgress(preferences.getInt(SYMBOL_SIZE, symbolSizeSeekBar.valueToProgress(1000)));
        generationSizeSeekBar.setProgress(preferences.getInt(GENERATION_SIZE, generationSizeSeekBar.valueToProgress(64)));
        generationWindowSizeSeekBar.setProgress(preferences.getInt(GENERATION_WINDOW_SIZE, generationWindowSizeSeekBar.valueToProgress(20)));
        dataRedundancySeekBar.setProgress(preferences.getInt(DATA_REDUNDANCY, dataRedundancySeekBar.valueToProgress(10)));
        feedbackProbabilitySeekBar.setProgress(preferences.getInt(FEEDBACK_PROBABILITY, feedbackProbabilitySeekBar.valueToProgress(50)));


        mServer.setHandler(new Server.IServerHandler() {

            @Override
            public void onStarted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeState(State.connected);
                    }
                });
                mMessageGenerator.start();
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
                mMessageGenerator.stop();
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        mServer.setHandler(null);
        mMessageGenerator.stop();
        mServer.stop();

        getSharedPreferences(SENDER_CONFIGURATION, MODE_PRIVATE)
                .edit()
                .putString(SENDER_PORT, portEditText.getText().toString())
                .putString(SENDER_IP, ipEditText.getText().toString())
                .putInt(SENDER_SPEED, speedSeekBar.getProgress())
                .putInt(SENDER_BUFFER_SIZE, bufferSizeSeekBar.getProgress())
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

    @Override
    public void onMessage(byte[] data) {
        try {
            mServer.sendData(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        speedSeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.speedSeekBar),
                (TextView)findViewById(R.id.speedTextView),
                false);
        speedSeekBar.setMax(MessageGenerator.MAX_SPEED);
        speedSeekBar.setMin(1);

        speedSeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {
            @Override
            public void onProgressChanged(double speed) {
                mMessageGenerator.setGeneratorSpeed((int)speed);
            }
        });

        bufferSizeSeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.bufferSizeSeekBar),
                (TextView)findViewById(R.id.bufferSizeTextView),
                false);
        bufferSizeSeekBar.setMax(MessageGenerator.MAX_MESSAGE_SIZE);
        bufferSizeSeekBar.setMin(MessageGenerator.MIN_MESSAGE_SIZE);

        bufferSizeSeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {

            @Override
            public void onProgressChanged(double bufferSize) {
                mMessageGenerator.setMessageSize((int)bufferSize);
            }
        });

        symbolSizeSeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.symbolSizeSeekBar),
                (TextView)findViewById(R.id.symbolSizeTextView),
                false);
        symbolSizeSeekBar.setMax(Sender.MAX_SYMBOL_SIZE);
        symbolSizeSeekBar.setMin(5);

        symbolSizeSeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {

            @Override
            public void onProgressChanged(double symbolSize) {
                mScoreEncoder.getScoreSender().setSymbolSize((int)symbolSize);
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
                mScoreEncoder.getScoreSender().setGenerationSize((int)generationSize);
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
                mScoreEncoder.getScoreSender().setGenerationWindowSize((int)generationWindowSize);
            }
        });

        dataRedundancySeekBar = new SeekBarHelper(
                (SeekBar)findViewById(R.id.dataRedundancySeekBar),
                (TextView)findViewById(R.id.dataRedundancyTextView),
                true);
        dataRedundancySeekBar.setMax(500);
        dataRedundancySeekBar.setMin(0);

        dataRedundancySeekBar.setOnProgressChangedListener(new SeekBarHelper.onProgressChangedListener() {
            @Override
            public void onProgressChanged(double dataRedundancy) {
                mScoreEncoder.getScoreSender().setDataRedundancy((float)dataRedundancy / 100);
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
                mScoreEncoder.getScoreSender().setFeedbackProbability((float)feedbackProbability / 100);
            }
        });
    }
}
