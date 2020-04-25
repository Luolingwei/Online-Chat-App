package com.imooc.netty;

import com.imooc.SpringUtil;
import com.imooc.enums.MsgActionEnum;
import com.imooc.service.Impl.UserServiceImpl;
import com.imooc.service.UserService;
import com.imooc.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 处理消息的handler
 * TextWebSocketFrame是netty中用于为websocket专门处理文本的对象, frame是消息的载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // 用于记录和管理所有客户端的channel
    public static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        /**
         * 客户端打开链接之后，获取客户端的channel并放到channelGroup中进行管理
         */
        users.add(ctx.channel());
        System.out.println("客户端建立连接，channel对应的长id为: " + ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        /**
         * 当触发handlerRemove, channelGroup会自动移除对应客户端的channel
         */
        users.remove(ctx.channel());
        System.out.println("客户端断开，channel对应的长id为: " + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常之后关闭链接(关闭channel), 随后从ChannelGroup中移除
        ctx.channel().close();
        users.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {

        // 获取客户端传输过来的消息
        String content = msg.text();
        Channel curChannel = ctx.channel();

        // 1. 获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content,DataContent.class);
        Integer action = dataContent.getAction();

        // 2. 判断消息类型，根据不同的类型来处理不同的业务
        if (action == MsgActionEnum.CONNECT.type){
            // 2.1 当websocket第一次open时，初始化channel，把用户的channel和userid关联起来
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId,curChannel);

//            // 测试
//            for (Channel channel: users){
//                System.out.println(channel.id().asLongText());
//            }
//            UserChannelRel.output();


        } else if (action == MsgActionEnum.CHAT.type) {
            // 2.2 聊天类型的消息, 把聊天记录保存到数据库, 同时标记消息的签收状态[未签收]
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();
            String senderId = chatMsg.getSenderId();

            // 保存消息到数据库, 并且标记为未签收
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);

            // 发送消息
            // 从全局用户channel关系中获取接收方的channel
            Channel receiverCahnnel = UserChannelRel.get(receiverId);
            if (receiverCahnnel == null){
                // TODO channel 为空代表用户离线, 推送消息(JPush, 小米推送)
                // 在手机主屏推送而不是app
            } else {
                // 当receiverChannel 不为空时，从ChannelGroup中去查找对应的channnel是否存在
                Channel findChannel = users.find(receiverCahnnel.id());
                if (findChannel!=null){
                    // 用户在线
                    /**
                     * 通过这个receiverCahnnel.writeAndFlush把消息推送给相应的用户，用户通过id和channel绑定
                     */
                    receiverCahnnel.writeAndFlush(
                            new TextWebSocketFrame(JsonUtils.objectToJson(dataContentMsg)));
                } else {
                    // 用户离线
                    // TODO 推送消息
                }
            }

        } else if (action == MsgActionEnum.SIGNED.type) {
            // 2.3 签收消息，针对具体的消息进行签收，修改数据库中对应消息的签收状态【已签收】
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");

            // 扩展字段在signed类型的消息中，代表需要去签收的消息id
            // 逗号间隔, 通过extend扩展字段传过来
            String msgIdStr = dataContent.getExtend();
            String msgIds[] = msgIdStr.split(",");

            List<String> msgIdList = new ArrayList<>();
            for (String msgId: msgIds){
                if (StringUtils.isNotBlank(msgId)){
                    msgIdList.add(msgId);
                }
            }

//            // 非空的msgId序列
//            System.out.println(msgIdList.toString());

            if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size()>0){
                // 批量签收消息
                userService.updateMsgSigned(msgIdList);
            }


        } else if (action == MsgActionEnum.KEEPALIVE.type){
            // 2.4 心跳类型的消息

        }






//        System.out.println("接收到的数据: " + content);
//
////        for (Channel channel : users){
////            channel.writeAndFlush(new TextWebSocketFrame("[服务器在]"
////                                                    + LocalDateTime.now()
////                                                    + "接收到消息, 消息为:"
////                                                    + content));
////        }
//        // 方法与上面for循环一致
//        users.writeAndFlush(new TextWebSocketFrame("[服务器在]"
//                + LocalDateTime.now()
//                + "接收到消息, 消息为:"
//                + content));

    }



}
