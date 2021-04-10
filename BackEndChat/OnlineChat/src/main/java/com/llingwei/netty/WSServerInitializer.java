package com.llingwei.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class WSServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(1024*64));

        // ===================above is used for supporting http====================//


        // =====================add support for heart beat=========================//


        // if client didn't send heart beat package to server in 1 min(All)，disconnect
        // do nothing for read/write idle
        pipeline.addLast(new IdleStateHandler(8, 10, 12));
        // self-defined idle status detection
        pipeline.addLast(new HeartBeatHandler());

        // =====================add support for heart beat=========================//


        // add support for websocket
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new ChatHandler());

    }
}
