package com.steinwurf.score;

import android.util.Log;

import com.steinwurf.score.sender.Sender;

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

    interface IOnDataHandler
    {
        void onData(byte[] data);
    }

    private final Sender encoder;

    private Thread mThread;

    // bytes per second
    private long speed = 1;
    private int messageSize = MIN_MESSAGE_SIZE;
    private int messageId = 0;
    private int packetId = 0;
    private int feedbackCount = 0;
    private boolean running = false;

    ScoreEncoder() {
        encoder = new Sender();
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

    void start(final IOnDataHandler onDataHandler)
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
                        encoderWriteData(message.array());
                        while (encoderHasOutgoingMessage())
                        {
                            ByteBuffer header = ByteBuffer.allocate(16);
                            header.order(ByteOrder.BIG_ENDIAN);
                            header.putInt(packetId); // 4
                            header.putLong(System.currentTimeMillis()); // 8
                            header.putInt(messageId); // 4
                            packetId++;

                            byte[] packetData = encoderGetOutgoingMessage();
                            ByteArrayOutputStream packet = new ByteArrayOutputStream();
                            try {
                                packet.write(header.array());
                                packet.write(packetData);
                                onDataHandler.onData(packet.toByteArray());
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

    private synchronized boolean encoderHasOutgoingMessage() {
        return encoder.hasOutgoingMessage();
    }

    private synchronized void encoderWriteData(byte[] data) {
        encoder.writeData(data);
    }

    private synchronized byte[] encoderGetOutgoingMessage() {
        return encoder.getOutgoingMessage();
    }

    synchronized void handleFeedback(byte[] data, int offset, int length) {
        try {
            encoder.receiveMessage(data, offset, length);
        } catch (Sender.InvalidFeedbackMessageException e) {
            e.printStackTrace();
        }
        feedbackCount++;
    }

    public int getFeedbackCount() {
        return feedbackCount;
    }

    synchronized void setSymbolSize(int symbolSize) {
        encoder.setSymbolSize(symbolSize);
    }

    synchronized void setGenerationSize(int generationSize) {
        encoder.setGenerationSize(generationSize);
    }

    synchronized void setGenerationWindowSize(int generationWindowSize) {
        encoder.setGenerationWindowSize(generationWindowSize);
    }

    synchronized void setDataRedundancy(float dataRedundancy) {
        encoder.setDataRedundancy(dataRedundancy);

    }

    synchronized void setFeedbackProbability(float feedbackProbability) {
        encoder.setFeedbackProbability(feedbackProbability);
    }


}
