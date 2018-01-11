package me.qsh.newborn.usbconnect.utils;

/**
 * 工具类 - byte
 * ============================================================================
 * 版权所有 2017 。
 *
 * @author fallenpanda
 * @version 1.0 2017-12-15 。
 * ============================================================================
 */
public class ByteUtils {

    /**
     * int to byte[2]
     *
     * @param i
     * @return
     */
    public static byte intToByte(int i) {
        return (byte) (i & 0xff);
    }

    /**
     * int to byte[2]
     *
     * @param i
     * @return
     */
    public static byte[] intToByte2(int i) {
        byte[] b = new byte[2];
        b[0] = (byte) (i & 0xff);
        b[1] = (byte) (i >> 8 & 0xff );
        return b;
    }

    /**
     * byte to unsigned int
     *
     * @param b
     * @return
     */
    public static int byteToInt(byte b) {
        return b & 0xff;
    }

    /**
     * byte[2] to int
     *
     * @param b
     * @return
     */
    public static int byte2ToInt(byte[] b) {
        int value = (b[0] & 0xff) | (b[1] & 0xff) << 8;
        if (value > 32767) {
            return byte2ToSignedInt(b);
        }
        return value;
    }

    /**
     * byte[2] to signed int
     *
     * @param b
     * @return
     */
    public static int byte2ToSignedInt(byte[] b) {
        return b[0] | b[1] << 8;
    }

    /**
     * byte 数组合并
     *
     * @param bytes
     * @return
     */
    public static byte[] mergeByte(byte[]... bytes) {
        int length = 0;
        for (byte[] bs : bytes) {
            length += bs.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] bs: bytes) {
            System.arraycopy(bs, 0, result, offset, bs.length);
            offset += bs.length;
        }
        return result;
    }

    /**
     * byte 数组合并进目标数组
     * （ 注意越界 ）
     *
     * @param target 目标数组
     * @param offset 起始位置
     * @param bytes 数组
     * @return
     */
    public static byte[] mergeByte(byte[] target, int offset, byte[]... bytes) {
        for (byte[] bs: bytes) {
            System.arraycopy(bs, 0, target, offset, bs.length);
            offset += bs.length;
        }
        return target;
    }

}
