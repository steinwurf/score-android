package com.steinwurf.score;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class SenderActivity extends AppCompatActivity {

    private static final String TAG = SenderActivity.class.getSimpleName();
    private static final String SENDER_CONFIGURATION = "SENDER_CONFIGURATION";
    private static final String SENDER_PORT = "SENDER_PORT";
    private static final String SENDER_IP = "SENDER_IP";
    private static final String SENDER_SPEED = "SENDER_SPEED";
    private static final String SENDER_BUFFER_SIZE = "SENDER_BUFFER_SIZE";

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
    private TextView speedTextView;
    private SeekBar speedSeekBar;
    private TextView bufferSizeTextView;
    private SeekBar bufferSizeSeekBar;

    WifiManager.MulticastLock multicastLock;

    private Server mServer;
    private DataGenerator mDataGenerator;

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
        speedTextView = findViewById(R.id.speedTextView);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        bufferSizeTextView = findViewById(R.id.bufferSizeTextView);
        bufferSizeSeekBar = findViewById(R.id.bufferSizeSeekBar);

        mServer = new Server();

        mDataGenerator = new DataGenerator(new DataGenerator.IDataGeneratorHandler() {
            @Override
            public void onData(byte[] data) {
                try {
                    mServer.sendData(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float percentage = i / (float)seekBar.getMax();
                int speed = i == 0 ? 1 : (int)(percentage * DataGenerator.MAX_SPEED);
                speedTextView.setText(Integer.toString(speed));
                mDataGenerator.setGeneratorSpeed(speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bufferSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float percentage = i / (float)seekBar.getMax();
                int bufferSize = (int)(percentage * (DataGenerator.MAX_BUFFER_SIZE - DataGenerator.MIN_BUFFER_SIZE) + DataGenerator.MIN_BUFFER_SIZE);
                bufferSizeTextView.setText(Integer.toString(bufferSize));
                mDataGenerator.setBufferSize(bufferSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SharedPreferences preferences = getSharedPreferences(SENDER_CONFIGURATION, MODE_PRIVATE);
        ipEditText.setText(preferences.getString(SENDER_IP, "224.0.0.251"));
        portEditText.setText(preferences.getString(SENDER_PORT, "9010"));

        /// onProgressChanged is only called when the progress is different from last.
        /// The default progess is 0, so changing it to 1 and then to whatever is returned
        /// from the preference will cause onProgressChanged, regardless of the value from the
        /// preference.
        speedSeekBar.setProgress(1);
        speedSeekBar.setProgress(preferences.getInt(SENDER_SPEED, 1000));

        bufferSizeSeekBar.setProgress(1);
        bufferSizeSeekBar.setProgress(preferences.getInt(SENDER_BUFFER_SIZE, 1000));

        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wm != null;
        multicastLock = wm.createMulticastLock(TAG);
        multicastLock.acquire();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mServer.setHandler(new Server.IServerHandler() {

            @Override
            public void onStarted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeState(State.connected);
                    }
                });
                mDataGenerator.start();
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
                mDataGenerator.stop();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeState(State.disconnected);
                    }
                });
            }
        });
        changeState(State.disconnected);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mServer.setHandler(null);
        mDataGenerator.stop();
        mServer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSharedPreferences(SENDER_CONFIGURATION, MODE_PRIVATE)
                .edit()
                .putString(SENDER_PORT, portEditText.getText().toString())
                .putString(SENDER_IP, ipEditText.getText().toString())
                .putInt(SENDER_SPEED, speedSeekBar.getProgress())
                .putInt(SENDER_BUFFER_SIZE, bufferSizeSeekBar.getProgress())
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
}
