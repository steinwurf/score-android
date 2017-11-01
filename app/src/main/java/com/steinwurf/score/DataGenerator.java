package com.steinwurf.score;

import android.util.Log;

import java.util.Random;

class DataGenerator {

    private static final String TAG = DataGenerator.class.getSimpleName();
    public static final int MAX_BUFFER_SIZE = 2000;
    public static final int MAX_SPEED = 10000; // bytes per second

    interface IDataGeneratorHandler
    {
        void onData(byte[] data);
    }

    private final Random random = new Random();
    private final IDataGeneratorHandler handler;

    private boolean running = false;

    // bytes per second
    private long speed = 1;
    private int bufferSize = 1;
    private byte id = 0;
    private Thread dataGeneratorThread;


    DataGenerator(IDataGeneratorHandler handler)
    {
        this.handler = handler;
    }

    void setBufferSize(int bufferSize)
    {
        assert bufferSize != 0;
        this.bufferSize = bufferSize;
    }

    void setGeneratorSpeed(long speed)
    {
        assert speed != 0;
        this.speed = speed;
    }

    void start()
    {
        Log.d(TAG, "start");
        running = true;
        dataGeneratorThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (running)
                    {
                        handler.onData(generateData(bufferSize));
                        long sleepTime = timeBetweenTransfers();
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        dataGeneratorThread.start();
    }

    void stop()
    {
        Log.d(TAG, "stop");
        running = false;
        if (dataGeneratorThread != null) {
            try {
                dataGeneratorThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    long timeBetweenTransfers()
    {
        return (long)((float)bufferSize / (float)speed * 1000.0);
    }

    private byte[] generateData(int size)
    {
        byte[] data = new byte[size];
        random.nextBytes(data);
        data[0] = id;
        id++;
        return data;
    }
}
