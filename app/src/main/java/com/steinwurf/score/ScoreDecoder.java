package com.steinwurf.score;

import android.util.Log;

import com.steinwurf.score.receiver.Receiver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScoreDecoder {

    private static final String TAG = ScoreDecoder.class.getSimpleName();

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

    private final Receiver decoder = new Receiver();

    void handleData(ByteBuffer data) {
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

        decoder.receiveMessage(data.array(), data.position(), data.remaining());
        while (decoder.dataAvailable())
        {
            byte[] message = decoder.getData();

            handleMessage(ByteBuffer.wrap(message));
        }
    }

    private void handleMessage(ByteBuffer message) {
        message.order(ByteOrder.BIG_ENDIAN);
        long messageId = message.getInt() & 0x00000000ffffffffL;
        long messageTimestamp = message.getLong();
        lastMessageSize = message.limit();
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
}

