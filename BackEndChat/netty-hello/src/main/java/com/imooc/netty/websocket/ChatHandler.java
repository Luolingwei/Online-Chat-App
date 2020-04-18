package com.imooc.netty.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;

/**
 * 处理消息的handler
 * TextWebSocketFrame是netty中用于为websocket专门处理文本的对象, frame是消息的载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // 用于记录和管理所有客户端的channel
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        /**
         * 客户端打开链接之后，获取客户端的channel并放到channelGroup中进行管理
         */
        clients.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        /**
         * 当触发handlerRemove, channelGroup会自动移除对应客户端的channel
         */
//        clients.remove(ctx.channel());
        System.out.println("客户端断开，channel对应的长id为: " + ctx.channel().id().asLongText());
        System.out.println("客户端断开，channel对应的短id为: " + ctx.channel().id().asShortText());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {

        // 获取客户端传输过来的消息
        String content = msg.text();
        System.out.println("接收到的数据: " + content);

//        for (Channel channel : clients){
//            channel.writeAndFlush(new TextWebSocketFrame("[服务器在]"
//                                                    + LocalDateTime.now()
//                                                    + "接收到消息, 消息为:"
//                                                    + content));
//        }
        // 方法与上面for循环一致
        clients.writeAndFlush(new TextWebSocketFrame("[服务器在]"
                + LocalDateTime.now()
                + "接收到消息, 消息为:"
                + content));

    }
}
