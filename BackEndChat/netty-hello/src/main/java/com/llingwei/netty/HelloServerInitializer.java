package com.llingwei.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * 初始化器, channel注册后，会执行里面相应的初始化方法 (逐个添加handler)
 */
public class HelloServerInitializer extends ChannelInitializer<SocketChannel> {


    protected void initChannel(SocketChannel channel) throws Exception {

        // 通过SocketChannel去获得对应的管道
        ChannelPipeline pipeline = channel.pipeline();

        // 通过管道添加handler
        // HttpServerCodec是netty自己提供的一个助手类
        // 当请求到服务端的时候，需要做解码，响应到客户端做编码
        pipeline.addLast("HttpServerCodec", new HttpServerCodec());

        // 添加自定义的助手类，返回"hello netty"
        pipeline.addLast("customHandler", new customHandler());


    }


}
