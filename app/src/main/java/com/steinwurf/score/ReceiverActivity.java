package com.steinwurf.score;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.steinwurf.score.receiver.Receiver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ReceiverActivity extends AppCompatActivity implements Client.IClientHandler {

    private static final String TAG = SenderActivity.class.getSimpleName();
    public static final int PORT = 8123;

    Button connectButton;
    private View.OnClickListener connectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            connectButton.setEnabled(false);
            connectButton.setOnClickListener(null);
            String ipString = ((TextView) findViewById(R.id.ipEditText)).getText().toString();
            int port = Integer.parseInt(((TextView) findViewById(R.id.portEditText)).getText().toString());
            try {
                InetAddress ip = InetAddress.getByName(ipString);
                mClient.start(ip, port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener disconnectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            connectButton.setEnabled(false);
            connectButton.setOnClickListener(null);
            try {
                mClient.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Client mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        setTitle(getString(R.string.receiver));
        ((TextView)findViewById(R.id.portEditText)).setText(Integer.toString(PORT));
        ((TextView)findViewById(R.id.ipEditText)).setText("");

        mClient = new Client();

        connectButton = findViewById(R.id.connectButton);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.setHandler(this);
        connectButton.setOnClickListener(connectOnClickListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mClient.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectButton.setEnabled(true);
                connectButton.setText(R.string.disconnect);
                connectButton.setOnClickListener(disconnectOnClickListener);
            }
        });
    }

    @Override
    public void onStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectButton.setEnabled(true);
                connectButton.setText(R.string.connect);
                connectButton.setOnClickListener(connectOnClickListener);
            }
        });
    }

    @Override
    public void onMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ReceiverActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
