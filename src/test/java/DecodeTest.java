import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.virtuemed.jt808.codec.JT808Decoder;
import net.virtuemed.jt808.codec.JT808Encoder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DecodeTest {

    /**
     * 服务端接收消息: 转义还原
     */
    @Test
    public void revert(){
        JT808Decoder jt808Decoder = new JT808Decoder();
        byte[] bytes = ByteBufUtil.decodeHexDump("307d02087d0155");
        ByteBuf revert = jt808Decoder.revert(bytes);
        String actual = ByteBufUtil.hexDump(revert);
        Assert.assertEquals("307e087d55",actual);
    }

    /**
     * 服务端发送消息: 转义
     */
    @Test
    public void escape(){
        JT808Encoder encoder = new JT808Encoder();
        byte[] bytes = ByteBufUtil.decodeHexDump("307e087d55");
        ByteBuf sendByteBuf = Unpooled.wrappedBuffer(bytes);
        ByteBuf revert = encoder.escape(sendByteBuf);
        String actual = ByteBufUtil.hexDump(revert);
        Assert.assertEquals(actual,"7e" + "307d02087d0155" + "7e");
    }
}
