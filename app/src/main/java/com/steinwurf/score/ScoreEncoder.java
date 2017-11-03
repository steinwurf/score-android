package com.steinwurf.score;

import com.steinwurf.score.sender.Sender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScoreEncoder implements MessageGenerator.IMessageGeneratorHandler {

    interface IOnMessageHandler
    {
        void onMessage(byte[] data);
    }

    private final IOnMessageHandler onMessageHandler;
    private final Sender mScoreSender = new Sender();

    private int id = 0;

    ScoreEncoder(IOnMessageHandler handler)
    {
        this.onMessageHandler = handler;
    }

    @Override
    public void onData(int messageId, byte[] data) {
        mScoreSender.writeData(data);
        while (mScoreSender.hasOutgoingMessage())
        {
            ByteBuffer header = ByteBuffer.allocate(16);
            header.order(ByteOrder.BIG_ENDIAN);
            header.putInt(id); // 4
            header.putLong(System.currentTimeMillis()); // 8
            header.putInt(messageId); // 4
            id++;

            byte[] message = mScoreSender.getOutgoingMessage();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(header.array());
                outputStream.write(message);
                onMessageHandler.onMessage(outputStream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    Sender getScoreSender()
    {
        return mScoreSender;
    }
}
