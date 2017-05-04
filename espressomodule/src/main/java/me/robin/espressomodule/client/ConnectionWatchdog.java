package me.robin.espressomodule.client;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * Created by Administrator on 2017-05-04.
 */
public class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask {

    @Override
    public void run(Timeout timeout) throws Exception {

    }
}
