package com.steinwurf.score;

import java.util.Locale;

class Utils {

    private static final double BYTES_PER_GIGABYTE = 1000000000f;

    private static final double BYTES_PER_MEGABYTE = 1000000f;

    private static final double BYTES_PER_KILOBYTE = 1000f;

    static long lengthBetween(long lastId, long newId)
    {
        if (newId <= lastId)
            newId += 4294967296L; // 0xFFFFFFFF + 1 for handling loop around
        return (newId - lastId) - 1;
    }

    static String bytesToPrettyString(double bytes) {
        if (bytes >= BYTES_PER_GIGABYTE) {
            return String.format(Locale.US, "%.1f GB", bytes / BYTES_PER_GIGABYTE);
        } else if (bytes >= BYTES_PER_MEGABYTE) {
            return String.format(Locale.US, "%.1f MB", bytes / BYTES_PER_MEGABYTE);
        } else if (bytes >= BYTES_PER_KILOBYTE) {
            return String.format(Locale.US, "%.1f kB", bytes / BYTES_PER_KILOBYTE);
        } else {
            return String.format(Locale.US, "%.1f B", bytes);
        }
    }
}
