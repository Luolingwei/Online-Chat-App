package com.llingwei.netty;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


/**
 * handler to monitor channel's heart beat
 * extend ChannelInboundHandlerAdapter, so we don't need to implement channelRead0 method
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // judge whether evt is IdleStateEvent
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;

            // read idle
            if (event.state() == IdleState.READER_IDLE){
                System.out.println("enter read idle");
            } else if (event.state() == IdleState.WRITER_IDLE){
                System.out.println("enter write idle");
            } else if (event.state() == IdleState.ALL_IDLE){
                System.out.println("before channel close, users number is: " + ChatHandler.users.size());
                Channel channel = ctx.channel();
                // close unused channels
                channel.close();
                System.out.println("after channel close, users number is: " + ChatHandler.users.size());
            }

        }


    }

}
