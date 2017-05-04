package me.robin.espressomodule.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.internal.StringUtil;
import me.robin.espressomodule.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017-05-02.
 */
public class NettyClient implements Closeable {

    private String serverHost;

    private int port;

    private EventLoopGroup group;

    private Channel channel;

    private Bootstrap bootstrap;

    private ChannelInitializer<SocketChannel> channelInitializer;

    private NoticeListener noticeListener;

    private volatile boolean connected = false, connecting = false;

    private HashedWheelTimer timer = new HashedWheelTimer();

    public NettyClient(String host, int port, ChannelInitializer<SocketChannel> channelInitializer, NoticeListener noticeListener) {
        this.serverHost = host;
        this.port = port;
        this.channelInitializer = channelInitializer;
        this.noticeListener = noticeListener;
        this.group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(channelInitializer);
    }

    public synchronized void connect() {
        if (!this.connecting) {
            this.connected = false;
            this.connecting = true;
            Utils.showToast("开始连接服务器");
            ChannelFuture channelFuture = bootstrap.connect(serverHost, port);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    boolean succeed = future.isSuccess();
                    if (!succeed) {
                        System.out.println("连接失败");
                        connecting = false;
                        timer.newTimeout(new TimerTask() {
                            @Override
                            public void run(Timeout timeout) throws Exception {
                                connect();
                            }
                        },3, TimeUnit.SECONDS);
                    } else {
                        connected = true;
                        connecting = false;
                        channel = future.channel();
                        synchronized (NettyClient.this) {
                            NettyClient.this.notifyAll();
                        }
                    }
                }
            });
        }
    }

    public void connectDelay(long delay) {
        this.group.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }, delay, TimeUnit.SECONDS);
    }

    public void sendRequest(String msg) {
        if (!connected) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ByteBuf byteBuf = null;
        try {
            byte[] data = null;
            if (!StringUtil.isNullOrEmpty(msg)) {
                data = msg.getBytes(Charset.forName("utf-8"));
            }
            byteBuf = channel.alloc().buffer();
            byteBuf.retain();
            int contentLength = null != data ? data.length : 0;
            byteBuf.writeInt(5 + contentLength);
            byteBuf.writeByte(CusHeartBeatHandler.CUSTOM_MSG);
            if (null != data) {
                byteBuf.writeBytes(data, 0, data.length);
            }
            channel.writeAndFlush(byteBuf);
        } finally {
            if (null != byteBuf) {
                byteBuf.release();
            }
        }
    }

    @Override
    public void close() throws IOException {
        group.shutdownGracefully();
    }

    public interface NoticeListener {
        void notice(String message);
    }
}
