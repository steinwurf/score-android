package com.steinwurf.score;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

class Client {

    private static final String TAG = Client.class.getSimpleName();

    interface IClientHandler
    {
        void onStarted();
        void onError(String reason);
        void onStopped();
    }
    interface IOnDataHandler
    {
        void onData(SocketAddress senderAddress, ByteBuffer data);
    }

    private IClientHandler clientHandler;
    private IOnDataHandler onDataHandler;
    private MulticastSocket socket;

    private final byte[] receiveBuffer = new byte[2000];

    private Thread connectionHandler = null;

    void setClientHandler(IClientHandler handler)
    {
        this.clientHandler = handler;
    }
    void setOnDataHandler(IOnDataHandler handler)
    {
        this.onDataHandler = handler;
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
                    if (clientHandler != null)
                        clientHandler.onStarted();

                    /// Read
                    while(true)
                    {
                        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(packet);

                        if (onDataHandler != null) {
                            ByteBuffer buffer = ByteBuffer.wrap(
                                    packet.getData(),
                                    packet.getOffset(),
                                    packet.getLength());
                            onDataHandler.onData(packet.getSocketAddress(), buffer);
                        }
                    }

                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace();
                    Log.d(TAG, "stopped");
                    if (clientHandler != null) {
                        clientHandler.onError(e.toString());
                        clientHandler.onStopped();
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
                clientHandler.onError(e.toString());
            }
        }
        Log.d(TAG, "stopped");
        if (clientHandler != null)
            clientHandler.onStopped();
    }
}
