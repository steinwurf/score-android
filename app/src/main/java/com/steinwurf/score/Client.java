package com.steinwurf.score;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

class Client {

    private static final String TAG = Client.class.getSimpleName();

    interface IClientHandler
    {
        void onStarted();
        void onError(String reason);
        void onStopped();
    }

    private IClientHandler clientHandler;
    private MulticastSocket socket;

    private final ScoreDecoder decoder;
    private final byte[] receiveBuffer = new byte[4096];
    private Thread connectionThread = null;

    Client(ScoreDecoder scoreDecoder)
    {
        decoder = scoreDecoder;
    }

    void setClientHandler(IClientHandler handler)
    {
        this.clientHandler = handler;
    }

    void start(final String ipString, final String portString) {
        connectionThread = new Thread(new Runnable() {
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
                    while(!socket.isClosed())
                    {
                        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(packet);

                        ByteBuffer buffer = ByteBuffer.wrap(
                                packet.getData(),
                                packet.getOffset(),
                                packet.getLength());
                        ArrayList<byte[]> snackPackets = decoder.handleData(buffer);
                        for (byte[] snackPacket : snackPackets) {
                            socket.send(new DatagramPacket(snackPacket, snackPacket.length, packet.getSocketAddress()));
                        }
                    }
                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace();
                    if (clientHandler != null)
                        clientHandler.onError(e.toString());
                } finally {
                    Log.d(TAG, "stopped");
                    if (clientHandler != null)
                        clientHandler.onStopped();
                }
            }
        });
        connectionThread.start();
    }

    void stop()
    {
        if (connectionThread != null) {
            socket.close();
            try {
                connectionThread.join();
            } catch (InterruptedException e) {
                clientHandler.onError(e.toString());
            }
        }
    }
}
