package com.steinwurf.score;

import android.util.Log;

import com.steinwurf.score.receiver.Receiver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class ScoreDecoder implements Client.IOnDataHandler {

    private static final String TAG = ScoreDecoder.class.getSimpleName();

    interface IOnMessageHandler
    {
        void onMessage(ByteBuffer data);
    }

    private final IOnMessageHandler onMessageHandler;
    private final Receiver mScoreReceiver = new Receiver();

    ScoreDecoder(IOnMessageHandler handler)
    {
        this.onMessageHandler = handler;
    }

    @Override
    public void onData(SocketAddress senderAddress, ByteBuffer buffer) {
        Log.d(TAG, "got data");
        mScoreReceiver.receiveMessage(buffer.array(), buffer.position(), buffer.remaining());
        if (!mScoreReceiver.dataAvailable())
            Log.d(TAG, "no message ready");
        while (mScoreReceiver.dataAvailable())
        {
            Log.d(TAG, "message ready!");
            byte[] message = mScoreReceiver.getData();
            onMessageHandler.onMessage(ByteBuffer.wrap(message));
        }
    }
}
