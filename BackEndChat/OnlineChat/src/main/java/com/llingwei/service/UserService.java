package com.llingwei.service;

import com.llingwei.netty.ChatMsg;
import com.llingwei.pojo.Users;
import com.llingwei.pojo.vo.FriendRequestVO;
import com.llingwei.pojo.vo.MyFriendsVO;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public interface UserService {

    // judge whether username exist
    public boolean queryUsernameExist(String username);

    // judge whether user's login succeed according to username and password
    public Users queryUserLoginSuccess(String username, String password);

    // save newly registered user
    public Users saveUser(Users user);

    // update this user's info to DB
    public Users updateUser(Users user);

    // do preCheck before really do a search friend query
    public Integer SearchFriendsPreCheck(String myUserId, String friendUsername);

    // query user object according to user name
    public Users querUserbyName(String username);

    // add friend request to DB
    public void sendFriendRequest(String myUserId, String friendUsername);

    // query friend request list by user id
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    // delete a friend request
    public void deleteFriendRequest(String acceptUserId, String senderUserId);

    // accept a friend request
    public void acceptFriendRequest(String acceptUserId, String senderUserId);

    // query user's friend list
    public List<MyFriendsVO> queryFriendList(String userId);

    // save chatMsg to DB
    public String saveMsg(ChatMsg chatMsg) throws NoSuchAlgorithmException, InvalidKeySpecException;

    // update sign status in batch
    public void updateMsgSignStatus(List<String> msgIdList);

    // query user's unsigned msg list
    public List<com.llingwei.pojo.ChatMsg> getUnSignedMsgList(String acceptUserId) throws NoSuchAlgorithmException, InvalidKeySpecException;

    // query user's public key according to userId
    public String searchPublicKey (String userId);
}
