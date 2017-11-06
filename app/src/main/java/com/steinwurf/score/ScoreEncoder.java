package com.steinwurf.score;

import android.util.Log;

import com.steinwurf.score.sender.Sender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

class ScoreEncoder {

    private static final String TAG = ScoreEncoder.class.getSimpleName();
    static final int MAX_MESSAGE_SIZE = 2000;
    static final int MIN_MESSAGE_SIZE = 4 + 8;
    static final int MAX_SPEED = 20000; // bytes per second

    interface IOnDataHandler
    {
        void onData(byte[] data);
    }

    private final Random random = new Random();
    private final Sender encoder;

    private Thread mThread;

    // bytes per second
    private long speed = 1;
    private int messageSize = MIN_MESSAGE_SIZE;
    private int messageId = 0;
    private int packetId = 0;
    private boolean running = false;

    public ScoreEncoder(Sender scoreEncoder) {
        encoder = scoreEncoder;
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

    void start(final IOnDataHandler handler)
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

                        ByteBuffer buffer = ByteBuffer.allocate(messageSize);
                        random.nextBytes(buffer.array());
                        buffer.order(ByteOrder.BIG_ENDIAN);

                        buffer.putInt(messageId); // 4
                        buffer.putLong(System.currentTimeMillis()); // 8
                        assert buffer.position() == MIN_MESSAGE_SIZE;
                        messageId++;
                        encoder.writeData(buffer.array());
                        while (encoder.hasOutgoingMessage())
                        {
                            ByteBuffer header = ByteBuffer.allocate(16);
                            header.order(ByteOrder.BIG_ENDIAN);
                            header.putInt(packetId); // 4
                            header.putLong(System.currentTimeMillis()); // 8
                            header.putInt(messageId); // 4
                            packetId++;

                            byte[] message = encoder.getOutgoingMessage();
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            try {
                                outputStream.write(header.array());
                                outputStream.write(message);
                                handler.onData(outputStream.toByteArray());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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
}
