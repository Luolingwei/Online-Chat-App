package com.llingwei.service.Impl;

import com.llingwei.enums.MsgActionEnum;
import com.llingwei.enums.MsgSignFlagEnum;
import com.llingwei.enums.SearchFriendsStatusEnum;
import com.llingwei.mapper.*;
import com.llingwei.netty.ChatMsg;
import com.llingwei.netty.DataContent;
import com.llingwei.netty.UserChannelRel;
import com.llingwei.pojo.FriendsRequest;
import com.llingwei.pojo.MyFriends;
import com.llingwei.pojo.Users;
import com.llingwei.pojo.vo.FriendRequestVO;
import com.llingwei.pojo.vo.MyFriendsVO;
import com.llingwei.service.UserService;
import com.llingwei.utils.JsonUtils;
import com.llingwei.utils.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.io.File;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final String FILE_SPACE = "/Users/luolingwei/Desktop/Program/OnlineChatApp/Online-Chat-App/UserFilesDB";

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private UsersMapperCustom usersMapperCustom;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameExist(String username) {

        Users user = new Users();
        user.setUsername(username);
        Users result = usersMapper.selectOne(user);
        return result != null;

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserLoginSuccess(String username, String password) {


        Example userExample = new Example(Users.class);
        Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username",username);
        criteria.andEqualTo("password",password);
        Users result = usersMapper.selectOneByExample(userExample);
        return result;


    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) {

        String userId = sid.nextShort();

        // generate unique QR Code for each user

        String uploadPathDB = "/" + userId + "/qrcode/qrcode.png";
        String localqrCodePath = FILE_SPACE + uploadPathDB;
        String dirPath = FILE_SPACE + "/" + userId + "/qrcode";

        File file = new File(dirPath);
        file.mkdirs();

        // InstaChat_qrcode:[username]
        qrCodeUtils.createQRCode(localqrCodePath,"InstaChat_qrcode:" + user.getUsername());
        user.setQrcode(uploadPathDB);
        user.setId(userId);
        usersMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUser(Users user) {

        usersMapper.updateByPrimaryKeySelective(user);
        return queryUserById(user.getId());

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    private Users queryUserById(String userId){
        return usersMapper.selectByPrimaryKey(userId);
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer SearchFriendsPreCheck(String myUserId, String friendUsername) {

        // 1 searched user doesn't exist，return NOT_EXIST
        Users result = querUserbyName(friendUsername);
        if (result == null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }

        // 2 search himself，return NOT_YOURSELF
        if (myUserId.equals(result.getId())){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }

        // 3 search user who is already in his friend list，return ALREADY_FRIENDS
        Example example = new Example(MyFriends.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("myUserId",myUserId);
        criteria.andEqualTo("myFriendUserId",result.getId());
        MyFriends friendRelation = myFriendsMapper.selectOneByExample(example);
        if (friendRelation!=null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }

        return SearchFriendsStatusEnum.SUCCESS.status;

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users querUserbyName(String username){

        Example example = new Example(Users.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",username);
        Users result = usersMapper.selectOneByExample(example);
        return result;

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {

        // query friend Info by userName
        Users friend = querUserbyName(friendUsername);

        // 1 query whether this request already exists
        Example example = new Example(FriendsRequest.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId",myUserId);
        criteria.andEqualTo("acceptUserId",friend.getId());
        FriendsRequest request = friendsRequestMapper.selectOneByExample(example);

        if (request==null){
            // request doesn't exist，add new request, do nothing if exist
            FriendsRequest savedRequest = new FriendsRequest();
            savedRequest.setId(sid.nextShort());
            savedRequest.setSendUserId(myUserId);
            savedRequest.setAcceptUserId(friend.getId());
            savedRequest.setRequestDataTime(new Date());
            friendsRequestMapper.insert(savedRequest);
        }

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {

        return usersMapperCustom.queryFriendRequestList(acceptUserId);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    // ignore friend request
    public void deleteFriendRequest(String acceptUserId, String senderUserId) {

        Example example = new Example(FriendsRequest.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId",senderUserId);
        criteria.andEqualTo("acceptUserId",acceptUserId);
        friendsRequestMapper.deleteByExample(example);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    // accept friend request
    public void acceptFriendRequest(String acceptUserId, String senderUserId) {

        /**
         * a friend request is accepted
         * 1 save friend
         * 2 save friend in reverse order
         * 3 delete this friend request
         */
        saveFriends(acceptUserId,senderUserId);
        saveFriends(senderUserId,acceptUserId);
        deleteFriendRequest(acceptUserId,senderUserId);

        // after accepting friend request, server send msg to sender via websocket, in order to update his contact
        Channel sendChannel = UserChannelRel.get(senderUserId);
        if (sendChannel!=null){
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
            sendChannel.writeAndFlush(
                    new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }


    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void saveFriends(String acceptUserId, String senderUserId){

        MyFriends myFriends = new MyFriends();
        myFriends.setId(sid.nextShort());
        myFriends.setMyUserId(acceptUserId);
        myFriends.setMyFriendUserId(senderUserId);
        myFriendsMapper.insert(myFriends);

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<MyFriendsVO> queryFriendList(String userId) {
        return usersMapperCustom.queryMyFriends(userId);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveMsg(ChatMsg chatMsg) {

        String msgId = sid.nextShort();
        com.llingwei.pojo.ChatMsg msgDB = new com.llingwei.pojo.ChatMsg();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());
        chatMsgMapper.insert(msgDB);
        return msgId;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateMsgSignStatus(List<String> msgIdList) {

        usersMapperCustom.updateMsgSigned(msgIdList);

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<com.llingwei.pojo.ChatMsg> getUnSignedMsgList(String acceptUserId) {

        Example example = new Example(com.llingwei.pojo.ChatMsg.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("acceptUserId",acceptUserId);
        criteria.andEqualTo("signFlag",0);
        return chatMsgMapper.selectByExample(example);

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public String searchPublicKey(String userId) {

        Users user = queryUserById(userId);
        return user.getPublicKey();

    }


}
