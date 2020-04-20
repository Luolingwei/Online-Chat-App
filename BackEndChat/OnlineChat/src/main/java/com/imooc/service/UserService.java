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


}
