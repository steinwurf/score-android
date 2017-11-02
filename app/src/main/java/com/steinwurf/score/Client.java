package com.steinwurf.score;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;

class Client {

    private static final String TAG = Client.class.getSimpleName();

    interface IClientHandler
    {
        void onStarted();
        void onError(String reason);
        void onStopped();
        void onData(ByteBuffer data);
    }

    private IClientHandler handler;
    private MulticastSocket socket;

    private final byte[] receiveBuffer = new byte[2000];

    private Thread connectionHandler = null;

    void setHandler(IClientHandler handler)
    {
        this.handler = handler;
    }

    void start(final String ipString, final String portString) {
        connectionHandler = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int port = Integer.parseInt(portString);
                    InetAddress ip = InetAddress.getByName(ipString);
                    socket = new MulticastSocket(port);
                    socket.joinGroup(ip);

                    Log.d(TAG, "started");
                    if (handler != null)
                        handler.onStarted();

                    /// Read
                    while(true)
                    {
                        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(packet);
                        if (handler != null) {
                            ByteBuffer buffer = ByteBuffer.wrap(
                                    packet.getData(),
                                    packet.getOffset(),
                                    packet.getLength());
                            handler.onData(buffer);
                        }
                    }

                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace();
                    Log.d(TAG, "stopped");
                    if (handler != null) {
                        handler.onError(e.toString());
                        handler.onStopped();
                    }
                }
            }
        });
        connectionHandler.start();
    }

    void stop()
    {
        if (connectionHandler != null) {
            socket.close();
            try {
                connectionHandler.join();
            } catch (InterruptedException e) {
                handler.onError(e.toString());
            }
        }
        Log.d(TAG, "stopped");
        if (handler != null)
            handler.onStopped();
    }
}
