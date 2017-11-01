package com.steinwurf.score;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ReceiverActivity extends AppCompatActivity {

    private static final String TAG = ReceiverActivity.class.getSimpleName();
    private static final String RECEIVER_CONFIGURATION = "RECEIVER_CONFIGURATION";
    private static final String RECEIVER_PORT = "RECEIVER_PORT";
    private static final String RECEIVER_IP = "RECEIVER_IP";

    enum State
    {
        connected,
        disconnected,
        intermediate
    }

    private Button connectButton;
    private EditText ipEditText;
    private EditText portEditText;
    private TextView statusTextView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        setTitle(getString(R.string.receiver));
        connectButton = findViewById(R.id.connectButton);
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);
        statusTextView = findViewById(R.id.statusTextView);

        SharedPreferences preferences = getSharedPreferences(RECEIVER_CONFIGURATION, MODE_PRIVATE);
        ipEditText.setText(preferences.getString(RECEIVER_IP, "224.0.0.251"));
        portEditText.setText(preferences.getString(RECEIVER_PORT, "9010"));

        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wm != null;
        multicastLock = wm.createMulticastLock(TAG);
        multicastLock.acquire();

        mClient = new Client();
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
            public void onData(final byte[] data) {
                final int id = data[0] & 0xff;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusTextView.setText(Integer.toString(id));
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSharedPreferences(RECEIVER_CONFIGURATION, MODE_PRIVATE)
                .edit()
                .putString(RECEIVER_PORT, portEditText.getText().toString())
                .putString(RECEIVER_IP, ipEditText.getText().toString())
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
                ipEditText.setEnabled(false);
                portEditText.setEnabled(false);
                break;
            case intermediate:
                ipEditText.setEnabled(false);
                portEditText.setEnabled(false);
                connectButton.setEnabled(false);
                connectButton.setOnClickListener(null);
                break;
            case disconnected:
                connectButton.setText(R.string.connect);
                connectButton.setOnClickListener(connectOnClickListener);
                connectButton.setEnabled(true);
                ipEditText.setEnabled(true);
                portEditText.setEnabled(true);
                break;
        }
    }
}
