package com.steinwurf.score;

import android.util.Log;

import com.steinwurf.score.source.Source;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class ScoreEncoder {

    private static final String TAG = ScoreEncoder.class.getSimpleName();
    static final int MAX_MESSAGE_SIZE = 10000;
    static final int MIN_MESSAGE_SIZE = 4 + 8;
    static final int MAX_SPEED = 100000; // bytes per second

    private boolean rateLimiterEnabled = true;
    public void enableRateLimiter(boolean enable) {
        rateLimiterEnabled = enable;
    }

    interface IOnDataPacketHandler
    {
        void onDataPacket(byte[] data);
    }

    private final Source source;

    private Thread mThread;

    // bytes per second
    private long speed = 1;
    private int messageSize = MIN_MESSAGE_SIZE;
    private int messageId = 0;
    private int packetId = 0;
    private int snackCount = 0;
    private boolean running = false;

    ScoreEncoder() {
        source = new Source();
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

    void start(final IOnDataPacketHandler onDataHandler)
    {
        Log.d(TAG, "start");
        running = true;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(running)
                {
                    try {
                        if (rateLimiterEnabled)
                            Thread.sleep(timeBetweenTransfers());

                        ByteBuffer message = ByteBuffer.allocate(messageSize);
                        message.order(ByteOrder.BIG_ENDIAN);

                        message.putInt(messageId); // 4
                        message.putLong(System.currentTimeMillis()); // 8
                        assert message.position() == MIN_MESSAGE_SIZE;
                        messageId++;
                        sourceReadMessage(message.array());
                        while (sourceHasDataPacket())
                        {
                            ByteBuffer header = ByteBuffer.allocate(16);
                            header.order(ByteOrder.BIG_ENDIAN);
                            header.putInt(packetId); // 4
                            header.putLong(System.currentTimeMillis()); // 8
                            header.putInt(messageId); // 4
                            packetId++;

                            byte[] dataPacket = sourceGetDataPacket();
                            ByteArrayOutputStream packet = new ByteArrayOutputStream();
                            try {
                                packet.write(header.array());
                                packet.write(dataPacket);
                                onDataHandler.onDataPacket(packet.toByteArray());
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

    private synchronized boolean sourceHasDataPacket() {
        return source.hasDataPacket();
    }

    private synchronized void sourceReadMessage(byte[] data) {
        source.readMessage(data);
    }

    private synchronized byte[] sourceGetDataPacket() {
        return source.getDataPacket();
    }

    synchronized void handleSnack(byte[] data, int offset, int length) {
        try {
            source.readSnackPacket(data, offset, length);
        } catch (Source.InvalidSnackPacketException e) {
            e.printStackTrace();
        }
        snackCount++;
    }

    public int getSnackCount() {
        return snackCount;
    }

    synchronized void setSymbolSize(int symbolSize) {
        source.setSymbolSize(symbolSize);
    }

    synchronized void setGenerationSize(int generationSize) {
        source.setGenerationSize(generationSize);
    }

    synchronized void setGenerationWindowSize(int generationWindowSize) {
        source.setGenerationWindowSize(generationWindowSize);
    }

    synchronized void setDataRedundancy(float dataRedundancy) {
        source.setDataRedundancy(dataRedundancy);

    }

    synchronized void setFeedbackProbability(float feedbackProbability) {
        source.setFeedbackProbability(feedbackProbability);
    }


}
