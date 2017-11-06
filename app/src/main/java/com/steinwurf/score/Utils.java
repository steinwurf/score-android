package com.steinwurf.score;

class Utils {

    public static long lengthBetween(long lastId, long newId)
    {
        if (newId <= lastId)
            newId += 4294967296L; // 0xFFFFFFFF + 1 for handling loop around
        return (newId - lastId) - 1;
    }
}
