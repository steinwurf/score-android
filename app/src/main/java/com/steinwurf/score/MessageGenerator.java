package com.steinwurf.score;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

class MessageGenerator {

    private static final String TAG = MessageGenerator.class.getSimpleName();
    public static final int MAX_MESSAGE_SIZE = 2000;
    public static final int MIN_MESSAGE_SIZE = 4 + 8;
    public static final int MAX_SPEED = 20000; // bytes per second

    interface IMessageGeneratorHandler
    {
        void onData(int id, byte[] data);
    }

    private Thread mThread;
    private final Random random = new Random();
    private final IMessageGeneratorHandler messageGeneratorHandler;

    // bytes per second
    private long speed = 1;
    private int messageSize = MIN_MESSAGE_SIZE;
    private int id = 0;
    private boolean running = false;

    MessageGenerator(IMessageGeneratorHandler handler)
    {
        this.messageGeneratorHandler = handler;
    }

    void setMessageSize(int messageSize)
    {
        assert messageSize >= MIN_MESSAGE_SIZE;
        this.messageSize = messageSize;
        if (mThread != null)
            mThread.interrupt();
    }

    void setGeneratorSpeed(long speed)
    {
        assert speed != 0;
        this.speed = speed;
        if (mThread != null)
            mThread.interrupt();
    }

    void start()
    {
        Log.d(TAG, "start");
        running = true;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(running)
                {
                    try {
                        Thread.sleep(timeBetweenTransfers());
                        ByteBuffer buffer = generateData(messageSize);
                        messageGeneratorHandler.onData(id, buffer.array());
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
        mThread.start();
    }

    void stop()
    {
        Log.d(TAG, "stop");
        running = false;
        if (mThread != null) {
            mThread.interrupt();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThread = null;
        }
    }

    private long timeBetweenTransfers()
    {
        return (long)((float) messageSize / (float)speed * 1000.0);
    }

    private ByteBuffer generateData(int size)
    {
        ByteBuffer buffer = ByteBuffer.allocate(size);

        random.nextBytes(buffer.array());
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putInt(id); // 4
        buffer.putLong(System.currentTimeMillis()); // 8
        assert buffer.position() == MIN_MESSAGE_SIZE;
        id++;
        return buffer;
    }
}
