package net.virtuemed.jt808.util;

import io.netty.buffer.ByteBuf;

/**
 * @Author: Zpsw
 * @Date: 2019-05-15
 * @Description:
 * @Version: 1.0
 */
public class JT808Util {

    /**
     * 根据byteBuf的readerIndex和writerIndex计算校验码
     * 校验码规则：从消息头开始，同后一字节异或，直到校验码前一个字节，占用 1 个字节
     * @param byteBuf
     * @return
     *
    ^是异或，相同为0，不同为1，例如int x = 19^20,
    19:0001 0011
    20:0001 0100
     7:0000 0111
     */
    public static byte XorSumBytes(ByteBuf byteBuf) {
        byte sum = byteBuf.getByte(byteBuf.readerIndex());
        for (int i = byteBuf.readerIndex() + 1; i < byteBuf.writerIndex(); i++) {
            sum = (byte) (sum ^ byteBuf.getByte(i));
        }
        return sum;
    }

}
