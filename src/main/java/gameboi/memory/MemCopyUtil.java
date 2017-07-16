package main.java.gameboi.memory;

/**
 */
public class MemCopyUtil {
    /**
     * Overloaded copyArray for saving
     *
     * @param src array
     * @param srcPos initial position
     * @param dst byte destination array
     * @param dstPos initial pos
     * @param len len of items to copy
     */
    public static void copyArray(int[] src, int srcPos, byte[] dst, int dstPos, int len) {
        for (int i = 0; i < len; ++i) {
            dst[i + dstPos] = (byte)(src[i + srcPos] & 0xff);
        }

    }

    /**
     * Overloaded copyArray for saving
     * @param src byte array
     * @param srcPos initial pos
     * @param dst int destination
     * @param dstPos initial pos
     * @param len len of items to copy
     */
    public static void copyArray(byte[] src, int srcPos, int[] dst, int dstPos, int len) {
        for (int i = 0; i < len; ++i) {
            dst[dstPos + i] = Byte.toUnsignedInt(src[i + srcPos]);
        }
    }
}
