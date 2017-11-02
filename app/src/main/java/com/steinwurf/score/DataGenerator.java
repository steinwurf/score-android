package com.steinwurf.score;

import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

class DataGenerator {

    private static final String TAG = DataGenerator.class.getSimpleName();
    public static final int MAX_BUFFER_SIZE = 2000;
    public static final int MIN_BUFFER_SIZE = 4 + 8;
    public static final int MAX_SPEED = 20000; // bytes per second

    interface IDataGeneratorHandler
    {
        void onData(byte[] data);
    }

    private final Handler mHandler = new Handler();
    private final Random random = new Random();
    private final IDataGeneratorHandler dataGeneratorHandler;
    private final Runnable createData = new Thread(new Runnable() {
        @Override
        public void run() {
            if (!running)
                return;

            mHandler.postDelayed(this, timeBetweenTransfers());
            ByteBuffer buffer = generateData(bufferSize);
            dataGeneratorHandler.onData(buffer.array());
        }
    });

    // bytes per second
    private long speed = 1;
    private int bufferSize = 1;
    private int id = 0;
    private boolean running = false;


    DataGenerator(IDataGeneratorHandler handler)
    {
        this.dataGeneratorHandler = handler;
    }

    void setBufferSize(int bufferSize)
    {
        assert bufferSize != 0;
        this.bufferSize = bufferSize;
        mHandler.removeCallbacks(createData);
        mHandler.postDelayed(createData, timeBetweenTransfers());
    }

    void setGeneratorSpeed(long speed)
    {
        assert speed != 0;
        this.speed = speed;
        mHandler.removeCallbacks(createData);
        mHandler.postDelayed(createData, timeBetweenTransfers());
    }

    void start()
    {
        Log.d(TAG, "start");
        running = true;
        mHandler.postDelayed(createData, timeBetweenTransfers());
    }

    void stop()
    {
        Log.d(TAG, "stop");
        running = false;
        mHandler.removeCallbacks(createData);
    }

    private long timeBetweenTransfers()
    {
        return (long)((float)bufferSize / (float)speed * 1000.0);
    }

    private ByteBuffer generateData(int size)
    {
        ByteBuffer buffer = ByteBuffer.allocate(size);

        random.nextBytes(buffer.array());
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putInt(id); // 4
        buffer.putLong(System.currentTimeMillis()); // 8
        id++;
        return buffer;
    }
}
