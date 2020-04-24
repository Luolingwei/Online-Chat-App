package com.imooc.service;

import com.imooc.netty.ChatMsg;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.FriendRequestVO;
import com.imooc.pojo.vo.MyFriendsVO;

import java.util.List;

public interface UserService {

    // 判断用户名是否存在
    public boolean queryUsernameIsExist(String username);

    // 根据用户名和密码查询用户是否存在
    public Users queryUserForLogin (String username, String password);

    // 用户注册
    public Users saveUser(Users user);

    // 更新用户信息到数据库
    public Users updateUserInfo(Users user);

    // 搜索朋友的前置条件
    public Integer preconditionSearchFriends(String myUserId, String friendUsername);

    // 根据用户名查询用户对象
    public Users querUserInfobyName(String username);

    // 添加好友请求的记录到数据库
    public void sendFriendRequest(String myUserId, String friendUsername);

    // 根据用户id查询其受到的好友请求
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    // 删除好友请求记录
    public void deleteFriendRequest(String acceptUserId, String senderUserId);

    /**
     * 通过好友请求
     * 1 保存好友
     * 2 逆向保存好友
     * 3 删除request记录
     */
    // 通过好友请求记录
    public void passFriendRequest(String acceptUserId, String senderUserId);

    // 查询用户的好友列表
    public List<MyFriendsVO> queryMyFriends(String userId);

    // 保存聊天消息到数据库
    public String saveMsg(ChatMsg chatMsg);

    // 批量更新消息签收状态
    public void updateMsgSigned (List<String> msgIdList);


}
