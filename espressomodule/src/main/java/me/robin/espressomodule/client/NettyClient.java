package me.robin.espressomodule.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by Administrator on 2017-05-02.
 */
public class NettyClient implements Closeable {

    private EventLoopGroup group;

    private Channel channel;

    private ChannelInitializer<SocketChannel> channelInitializer;

    private NoticeListener noticeListener;

    public NettyClient(ChannelInitializer<SocketChannel> channelInitializer, NoticeListener noticeListener) {
        this.channelInitializer = channelInitializer;
        this.noticeListener = noticeListener;
    }

    public void listen(String host, int port) throws InterruptedException {
        this.group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(channelInitializer);
        this.channel = b.connect(host, port).sync().channel();
    }

    public void sendRequest(Channel channel, String msg) {
        ByteBuf byteBuf = null;
        try {
            byte[] data = null;
            if (!StringUtil.isNullOrEmpty(msg)) {
                data = msg.getBytes(Charset.forName("utf-8"));
            }
            byteBuf = channel.alloc().buffer();
            byteBuf.retain();
            int contentLength = null!=data?data.length:0;
            this.noticeListener.notice("ByteLength:" + (null == data ? 0 : data.length) + " StrLength:" + contentLength);
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
