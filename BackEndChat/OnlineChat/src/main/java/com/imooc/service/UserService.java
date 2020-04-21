package com.imooc.service;

import com.imooc.pojo.Users;

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
}
