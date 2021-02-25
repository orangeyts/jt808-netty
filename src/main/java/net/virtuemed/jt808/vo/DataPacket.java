package net.virtuemed.jt808.vo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.virtuemed.jt808.util.BCD;
import net.virtuemed.jt808.config.JT808Const;
import org.apache.commons.lang3.StringUtils;

/**
 * & 两个操作数中位都为1，结果才为1，否则结果为0，例如下面的程序段。
 * | 两个位只要有一个为1，那么结果就是1，否则就为0，
 * ^ 异或运算符,两个操作数的位中，相同则结果为0，不同则结果为1
 * @Author: Zpsw
 * @Date: 2019-05-15
 * @Description:
 * @Version: 1.0
 */
@Data
@Slf4j
public class DataPacket {

    protected Header header = new Header(); //消息头
    protected ByteBuf payload; //消息体

    public DataPacket() {
    }

    public DataPacket(ByteBuf payload) {
        this.payload = payload;
    }

    public void parse() {
        try{
            this.parseHead();
            //验证包体长度
            if (this.header.getMsgBodyLength() != this.payload.readableBytes()) {
                throw new RuntimeException("包体长度有误");
            }
            this.parseBody();
        }finally {
            ReferenceCountUtil.safeRelease(this.payload);
        }
    }

    protected void parseHead() {
        header.setMsgId(payload.readShort());
        header.setMsgBodyProps(payload.readShort());
        header.setTerminalPhone(BCD.BCDtoString(readBytes(6)));
        header.setFlowId(payload.readShort());
        if (header.hasSubPackage()) {
            //TODO 处理分包
            payload.readInt();
        }
    }

    /**
     * 请求报文重写
     */
    protected void parseBody() {

    }

    /**
     * 响应报文重写 并调用父类
     * @return
     */
    public ByteBuf toByteBufMsg() {
        ByteBuf bb = ByteBufAllocator.DEFAULT.heapBuffer();//在JT808Encoder escape()方法处回收
        bb.writeInt(0);//先占4字节用来写msgId和msgBodyProps，JT808Encoder中覆盖回来
        bb.writeBytes(BCD.toBcdBytes(StringUtils.leftPad(this.header.getTerminalPhone(), 12, "0")));
        bb.writeShort(this.header.getFlowId());
        //TODO 处理分包
        return bb;
    }

    /**
     * 从ByteBuf中read固定长度的数组,相当于ByteBuf.readBytes(byte[] dst)的简单封装
     * @param length
     * @return
     */
    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        this.payload.readBytes(bytes);
        return bytes;
    }

    /**
     * 从ByteBuf中读出固定长度的数组 ，根据808默认字符集构建字符串
     * @param length
     * @return
     */
    public String readString(int length) {
       return new String(readBytes(length),JT808Const.DEFAULT_CHARSET);
    }

    @Data
    public static class Header {
        private short msgId;// 消息ID 2字节
        private short msgBodyProps;//消息体属性 2字节
        private String terminalPhone; // 终端手机号 6字节
        private short flowId;// 流水号 2字节

        //获取包体长度
        //1111111111
        public short getMsgBodyLength() {
            return (short) (msgBodyProps & 0x3ff);
        }

        //获取加密类型 3bits
        //1110000000000
        public byte getEncryptionType() {
            return (byte) ((msgBodyProps & 0x1c00) >> 10);
        }

        //是否分包
        //10000000000000
        public boolean hasSubPackage() {
            //String s = Integer.toBinaryString(msgBodyProps);
            //String s1 = Integer.toBinaryString(msgBodyProps & 0x2000);
            //log.info("消息体属性: [{}] : [{}]",s,s1);
            return ((msgBodyProps & 0x2000) >> 13) == 1;
        }
    }
}
