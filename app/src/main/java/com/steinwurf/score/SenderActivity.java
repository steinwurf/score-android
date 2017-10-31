package com.steinwurf.score;

        /*
         * show stats
         IP
        sender.dataRedundancy();
        sender.feedbackProbability();
        sender.generationSize();
        sender.generationWindowSize();
        sender.outgoingMessages();
        */
        /*
         * turn nobs
         sender.setDataRedundancy();
         sender.setSymbolSize();
         sender.setGenerationWindowSize();
         sender.setGenerationSize();
         sender.setFeedbackProbability();
         */

        /*
         * start
         * stop
         * version
         */
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

public class SenderActivity extends AppCompatActivity implements Server.IServerHandler {

    private static final String TAG = SenderActivity.class.getSimpleName();
    public static final int PORT = 8123;

    private Button connectButton;
    private View.OnClickListener connectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            connectButton.setEnabled(false);
            connectButton.setOnClickListener(null);
            try {
                int port = Integer.parseInt(((TextView) findViewById(R.id.portEditText)).getText().toString());
                mServer.start(port);
            } catch (NumberFormatException e) {
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
                mServer.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    private Server mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
        setTitle(getString(R.string.sender));
        connectButton = findViewById(R.id.connectButton);

        mServer = new Server();

        List<String> ipAddresses = getIpAddresses();
        if (ipAddresses.size() == 0)
        {
            ((TextView)findViewById(R.id.ipsTextView)).setText("Not connected");
        }
        else
        {
            StringBuilder stringBuilder = new StringBuilder();
            for (String ip : ipAddresses)
            {
                stringBuilder.append(ip);
                stringBuilder.append('\n');
            }
            ((TextView)findViewById(R.id.ipsTextView)).setText(stringBuilder.toString());
        }
        ((TextView)findViewById(R.id.portEditText)).setText(Integer.toString(PORT));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mServer.setHandler(this);
        connectButton.setOnClickListener(connectOnClickListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mServer.setHandler(null);
        try {
            mServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] generateData(int size)
    {
        byte[] data = new byte[size];
        new Random().nextBytes(data);
        return data;
    }

    private List<String> getIpAddresses()
    {
        List<String> ipAddresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ipAddresses.add(inetAddress.getHostAddress());
                    }
                }
            }
        }
        catch (SocketException ignored) {
        }
        return ipAddresses;
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
    public void onConnection(final String hostAddress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SenderActivity.this, hostAddress, Toast.LENGTH_LONG).show();
            }
        });
    }
}
