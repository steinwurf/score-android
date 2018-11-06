package com.steinwurf.score;

import android.util.Log;

import com.steinwurf.score.sink.Sink;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class ScoreDecoder {

    class Message {
        int size = 1500;
        byte[] data = new byte[size];
    }

    private static final String TAG = ScoreDecoder.class.getSimpleName();

    private boolean feedbackEnabled = false;

    // Packet
    private Long firstPacketId = null; // The ID of the first packet
    private Long lastPacketId = null; // The ID of the last packet
    private Long lastPacketTimestamp = null;
    private long packetBytesReceived = 0;
    private long packetsReceived = 0;
    private long packetsLost = 0;
    private long totalPackets = 0;
    private long lastPacketSize = 0;

    // Message
    private Long firstMessageId = null; // The ID of the first decoded message
    private Long lastMessageId = null; // The ID of the last decoded message
    private Long latestMessageId = null; // The ID of the message inside the last received packet
    private Long lastMessageTimestamp = null;
    private long messageBytesReceived = 0;
    private long messagesReceived = 0;
    private long messagesLost = 0;
    private long totalMessages = 0;
    private long lastMessageSize = 0;

    private final Sink sink = new Sink();
    private final Message message = new Message();

    ArrayList<byte[]> handleData(ByteBuffer data) {
        data.order(ByteOrder.BIG_ENDIAN);
        long packetId = data.getInt() & 0x00000000ffffffffL;
        long packetTimestamp = data.getLong();
        latestMessageId = data.getInt() & 0x00000000ffffffffL;
        lastPacketSize = data.remaining();
        packetBytesReceived += lastPacketSize;

        if (firstPacketId == null) {
            firstPacketId = packetId;
            totalPackets = 1;
        }
        else
        {
            totalPackets = Utils.lengthBetween(firstPacketId, packetId);
        }

        if (lastPacketTimestamp != null && lastPacketId != null)
        {
            if (lastPacketTimestamp > packetTimestamp)
            {
                Log.w(TAG, "Out of order packet received");
            }
            else
            {
                packetsLost += Utils.lengthBetween(lastPacketId, packetId);
            }
        }
        lastPacketTimestamp = packetTimestamp;
        lastPacketId = packetId;
        packetsReceived += 1;

        try {
            sink.readDataPacket(data.array(), data.position(), data.remaining());
        } catch (Sink.InvalidDataPacketException e) {
            e.printStackTrace();
        }
        while (sink.hasData())
        {
            if (message.data.length < sink.messageSize())
            {
                message.data = new byte[sink.messageSize()];
            }
            message.size = sink.messageSize();
            sink.writeToMessage(message.data);
            if (sink.messageCompleted())
                handleMessage(ByteBuffer.wrap(message.data, 0, message.size));
        }
        ArrayList<byte[]> snackPackets = new ArrayList<>();
        while (feedbackEnabled && sink.hasSnackPacket())
        {
            byte[] snackPacket = sink.getSnackPacket();
            snackPackets.add(snackPacket);
        }
        return snackPackets;
    }

    private void handleMessage(ByteBuffer message) {
        message.order(ByteOrder.BIG_ENDIAN);
        lastMessageSize = message.remaining();
        long messageId = message.getInt() & 0x00000000ffffffffL;
        long messageTimestamp = message.getLong();
        messageBytesReceived += lastMessageSize;

        if (firstMessageId == null) {
            firstMessageId = messageId;
            totalMessages = 1;
        }
        else
        {
            totalMessages = Utils.lengthBetween(firstMessageId, messageId);
        }

        if (lastMessageId != null)
        {
            messagesLost += Utils.lengthBetween(lastMessageId, messageId);
        }

        lastMessageTimestamp = messageTimestamp;
        lastMessageId = messageId;
        messagesReceived += 1;
    }

    void resetStats() {

        // Packet
        firstPacketId = null;
        lastPacketId = null;
        lastPacketTimestamp = null;
        packetBytesReceived = 0;
        packetsReceived = 0;
        packetsLost = 0;
        totalPackets = 0;
        lastPacketSize = 0;

        // Message
        firstMessageId = null;
        lastMessageId = null;
        latestMessageId = null;
        lastMessageTimestamp = null;
        messageBytesReceived = 0;
        messagesReceived = 0;
        messagesLost = 0;
        totalMessages = 0;
        lastMessageSize = 0;
    }

    float messageLossPercentage()
    {
        return totalMessages != 0 ? ((float) messagesLost / (float) totalMessages) * 100 : 0;
    }

    long getMessagesBehind()
    {
        return lastMessageId != null ? latestMessageId - lastMessageId : 0;
    }

    long getMessageBytesReceived() {
        return messageBytesReceived;
    }

    long getMessagesReceived() {
        return messagesReceived;
    }

    long getMessagesLost() {
        return messagesLost;
    }

    long getDelay() {
        return lastMessageTimestamp != null ? lastPacketTimestamp - lastMessageTimestamp : 0;
    }

    float getGoodPut() {
        return ((float) messageBytesReceived / (float) packetBytesReceived) * 100;
    }

    long getPacketsReceived() {
        return packetsReceived;
    }
    long getPacketsLost() {
        return packetsLost;
    }
    long getPacketBytesReceived() {
        return packetBytesReceived;
    }
    float packetLossPercentage()
    {
        return totalPackets == 0 ? 0 : ((float) packetsLost / (float) totalPackets) * 100;
    }


    public long getLastPacketSize() {
        return lastPacketSize;
    }

    public long getLastMessageSize() {
        return lastMessageSize;
    }

    public void setFeedbackEnabled(boolean feedbackEnabled) {
        this.feedbackEnabled = feedbackEnabled;
    }
}

