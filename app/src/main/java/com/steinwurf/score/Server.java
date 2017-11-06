package com.steinwurf.score;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class Server implements ScoreEncoder.IOnDataHandler {
    private static final String TAG = Server.class.getSimpleName();

    interface IServerHandler
    {
        void onStarted();
        void onError(String reason);
        void onStopped();
    }

    private final ScoreEncoder encoder;

    private IServerHandler handler;
    private MulticastSocket socket;
    private int port = 0;
    private InetAddress ip = null;

    Server(ScoreEncoder encoder) {
        this.encoder = encoder;
    }

    void setServerHandler(IServerHandler handler)
    {
        this.handler = handler;
    }

    void start(final String ipString, final String portString) {
        new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    port = Integer.parseInt(portString);
                    ip = InetAddress.getByName(ipString);
                    socket = new MulticastSocket(port);
                    socket.joinGroup(ip);

                    Log.d(TAG, "started");
                    if (handler != null)
                        handler.onStarted();

                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace();
                    if (handler != null) {
                        handler.onError(e.toString());
                    }
                    stop();
                }
            }
        }).start();
        encoder.start(this);
    }

    @Override
    public void onData(byte[] data) {

        if (socket != null)
        {
            DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void stop() {
        encoder.stop();
        if (socket != null) {
            socket.close();
        }

        Log.d(TAG, "stopped");
        if (handler != null) {
            handler.onStopped();
        }
    }
}
