package com.steinwurf.score;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class KeepAlive
{
    private static final String TAG = KeepAlive.class.getSimpleName();
    private final InetAddress host;
    private final int port;
    private int mInterval;
    private Thread mThread = null;
    private boolean mRunning = false;

    public KeepAlive(InetAddress host, int port, int interval)
    {
        this.host = host;
        this.port = port;
        mInterval = interval;
    }

    public void setInterval(int interval)
    {
        mInterval = interval;
    }

    public void start()
    {
        Log.d(TAG, "started: " + host + ":" + port);
        mThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                DatagramSocket socket = null;
                try
                {
                    socket = new DatagramSocket(null);
                    mRunning = true;
                    while (mRunning) {
                        byte[] buffer = {0x66};
                        DatagramPacket out = new DatagramPacket(buffer, buffer.length, host, port);
                        socket.send(out);
                        Thread.sleep(mInterval);
                    }
                }
                catch (IOException | InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally {
                    if (socket != null)
                        socket.close();
                }
            }
        });
        mThread.start();
    }

    public void stop()
    {
        Log.d(TAG, "stopped");
        mRunning = false;
        if (mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThread = null;
        }
    }
}
