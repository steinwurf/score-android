package com.steinwurf.score;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class KeepAlive
{
    private static final String TAG = "KeepAlive";
    private final InetAddress host;
    private final int port;
    private final int interval;
    private Thread mThread;
    private boolean mRunning;

    public KeepAlive(InetAddress host, int port, int interval)
    {
        this.host = host;
        this.port = port;
        this.interval = interval;
    }

    public void start()
    {
        Log.d(TAG, "started: " + host + ":" + port + " " + interval + "ms");
        mThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                DatagramSocket socket;
                try
                {
                    socket = new DatagramSocket(null);
                }
                catch (SocketException e)
                {
                    e.printStackTrace();
                    return;
                }

                mRunning = true;
                while (mRunning)
                {
                    byte[] buffer = {0x66};
                    DatagramPacket out = new DatagramPacket(buffer, buffer.length, host, port);
                    try
                    {
                        socket.send(out);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    try
                    {
                        Thread.sleep(interval);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                socket.close();
            }
        });
        mThread.start();
    }

    public void stop()
    {
        Log.d(TAG, "stopped");
        mRunning = false;
        try
        {
            mThread.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        mThread = null;
    }
}
