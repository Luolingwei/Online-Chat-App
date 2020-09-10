package com.llingwei.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 实现客户端发送一个请求，服务器会返回hello netty
 */
public class HelloServer {

    public static void main(String[] args) throws InterruptedException {

        // 定义一对线程组(两个线程池, 主从, 同步非阻塞)

        // 主线程组, 用于接收客户端的连接，但是不做任何处理
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        // 从线程组, 主线程组会把任务丢给他，让从线程组去处理
        EventLoopGroup workerGroup = new NioEventLoopGroup();


        try {
            // netty服务器的创建, serverBootstrap是一个启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup) // 设置主从线程组
                            .channel(NioServerSocketChannel.class) //设置nio双向通道
                            .childHandler(new HelloServerInitializer()); // 子处理器，用于处理workerGroup

            // 启动server, 并设置 8088为启动的端口号，且启动方式为同步
            ChannelFuture channelFuture = serverBootstrap.bind(8088).sync();

            // 用于监听关闭的channel, 设置同步方式
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 关闭主从线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

}
