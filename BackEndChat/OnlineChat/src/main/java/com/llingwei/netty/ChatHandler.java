package com.llingwei.netty;

import com.llingwei.SpringUtil;
import com.llingwei.enums.MsgActionEnum;
import com.llingwei.service.UserService;
import com.llingwei.utils.JsonUtils;
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
 * handler for msg handling
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // manage all channels of client
    public static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        /**
         * client open connection, obtain channel of client and put it in channelGroup
         */
        users.add(ctx.channel());
        System.out.println("client connection established，channel's long id: " + ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        /**
         * when trigger handlerRemove, channelGroup will remove client's channel
         */
        users.remove(ctx.channel());
        System.out.println("client disconnected，channel's long id: " + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
        users.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {

        // obtain msg from client
        String content = msg.text();
        Channel curChannel = ctx.channel();

        // 1. obtain msg from client
        DataContent dataContent = JsonUtils.jsonToPojo(content,DataContent.class);
        Integer action = dataContent.getAction();

        // 2. judge msg type, do different things according to the type
        if (action == MsgActionEnum.CONNECT.type){
            // 2.1 websocket open for the first time，initialize channel，connect channel with userId
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId,curChannel);

        } else if (action == MsgActionEnum.CHAT.type) {
            // 2.2 the type is chatMsg, save chatMsg to DB, mark sign status as unsigned
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();
            String senderId = chatMsg.getSenderId();

            // save chatMsg to DB, mark sign status as unsigned
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);

            // send msg
            // get receiver's channel from global UserChannelRel
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if (receiverChannel == null){

            } else {
                // when receiverChannel exist，query whether corresponding channel exist in ChannelGroup.
                Channel findChannel = users.find(receiverChannel.id());
                if (findChannel!=null){
                    // user is online
                    /**
                     * send msg to receiver via receiverCahnnel.writeAndFlush
                     */
                    receiverChannel.writeAndFlush(
                            new TextWebSocketFrame(JsonUtils.objectToJson(dataContentMsg)));
                }
            }

        } else if (action == MsgActionEnum.SIGNED.type) {
            // 2.3 sign msg，sign a specific msg，modify sign status to signed in DB
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");

            // extend field in Data Content, id is the msgId to be signed
            String msgIdStr = dataContent.getExtend();
            String msgIds[] = msgIdStr.split(",");

            List<String> msgIdList = new ArrayList<>();
            for (String msgId: msgIds){
                if (StringUtils.isNotBlank(msgId)){
                    msgIdList.add(msgId);
                }
            }

            if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size()>0){
                // sign msg in batch
                userService.updateMsgSignStatus(msgIdList);
            }


        } else if (action == MsgActionEnum.KEEPALIVE.type){
            // 2.4 heart beat
            System.out.println("received heart beat from channel[" +  curChannel   +"]...");
        }


    }


}
