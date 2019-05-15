package net.virtuemed.jt808.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: Zpsw
 * @Date: 2019-05-15
 * @Description:
 * @Version: 1.0
 */

@Slf4j
@Component
public class NettyTcpServer {

    @Value("${netty.port}")
    private int port;

    @Value("${netty.threads.boss}")
    private int bossThreadsNum;

    @Value("${netty.threads.worker}")
    private int workerThreadsNum;

    @Autowired
    private JT808ChannelInitializer jt808ChannelInitializer;

    private volatile boolean isStarted = false;
    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;


    public synchronized void start() {
        if (this.isStarted) {
            throw new IllegalStateException("TCP服务正在运行中，监听端口:" + port);
        }
        try {
            this.bossGroup = new NioEventLoopGroup(bossThreadsNum);
            this.workerGroup = new NioEventLoopGroup(workerThreadsNum);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(jt808ChannelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 1024) //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                    .childOption(ChannelOption.TCP_NODELAY, true)//立即写出
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//长连接
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);//内存泄漏检测 开发推荐PARANOID 线上SIMPLE
            this.isStarted = true;
            log.info("TCP服务启动完毕,port={}", this.port);
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("TCP服务启动出错:{}", e.getMessage());
        }
    }

}
