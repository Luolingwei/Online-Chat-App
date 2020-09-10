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

        // websocket基于http协议，所以要有http编解码器
        pipeline.addLast(new HttpServerCodec());
        // 对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());
        // 对http message进行聚合，聚合成FullHttpRequest或FullHttpResponse
        // 几乎在netty中的编程都会使用到此handler
        pipeline.addLast(new HttpObjectAggregator(1024*64));

        // ++++++++++++++++++++以上用于支持http协议++++++++++++++++++++++++//


        // ++++++++++++++++++++增加心跳支持 start++++++++++++++++++++++++//


        // 针对客户端, 如果在1分钟时，没有向服务端发送读写心跳(All)，则主动断开
        // 如果是读空闲，或者写空闲，不做处理
        pipeline.addLast(new IdleStateHandler(8, 10, 12));
        // 自定义的空闲状态检测
        pipeline.addLast(new HeartBeatHandler());



        // ++++++++++++++++++++增加心跳支持 end++++++++++++++++++++++++//


        // websocket服务器处理的协议，用于指定给客户端连接访问的路由: /ws
        /**
         * 本handler会处理繁重复杂的事件
         * ex: handshaking(close,ping,pong) ping + pong = 心跳
         * 对于websocket, 都是以frames传输，不同的数据类型对应的frames也不同
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new ChatHandler());

    }
}
