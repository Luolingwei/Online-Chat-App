package com.imooc.controller;

import com.imooc.enums.SearchFriendsStatusEnum;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UsersBO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.FileUtils;
import com.imooc.utils.IMoocJSONResult;
import com.imooc.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("u")
public class UserController extends BasicController{

    @Autowired
    private UserService userService;

    @PostMapping("/RegistOrLogin")
    public IMoocJSONResult RegistOrLogin(@RequestBody Users user) throws Exception {

        // 0 判断用户名和密码不能为空
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())){
            return IMoocJSONResult.errorMsg("用户名或密码不能为空...");
        }

        // 1 判断用户名是否存在，存在则登录，不存在则注册
        boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());

        Users userResult = null;
        if (usernameIsExist){
            // 1.1 登录
            userResult = userService.queryUserForLogin(user.getUsername(),MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null){
                return IMoocJSONResult.errorMsg("用户名或密码不正确...");
            }

        } else {
            // 1.2 注册
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult = userService.saveUser(user);
        }

        // 用userVO把不必要的字段滤掉，返回给前端
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        return IMoocJSONResult.ok(usersVO);
    }

    @PostMapping("/uploadFaceBase64")
    public IMoocJSONResult uploadFaceBase64(@RequestBody UsersBO usersBO) throws Exception {

        // 获取前端传的Base64字符串转换为文件对象存储到本地
        String base64Data = usersBO.getFaceData();
        String uploadPathDB = "/" + usersBO.getUserId() + "/face" + "/userface64.png";
        String finalPath = FILE_SPACE + uploadPathDB;
        String dirPath = FILE_SPACE + "/" + usersBO.getUserId() + "/face";

        System.out.println(uploadPathDB);
        System.out.println(finalPath);

        File file = new File(dirPath);
        file.mkdirs();

        // 将64位字符串转为file存到本地
        FileUtils.base64ToFile(finalPath,base64Data);

        // 更新数据库信息
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setFaceImage(uploadPathDB);
        user.setFaceImageBig(uploadPathDB);
        // 更新用户信息并把更新后的完整用户信息返回给前端
        Users result = userService.updateUserInfo(user);

        return IMoocJSONResult.ok(result);

    }

    @PostMapping("/setNickname")
    public IMoocJSONResult setNickname(@RequestBody UsersBO usersBO) throws Exception {

        // 更新数据库信息
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setNickname(usersBO.getNickName());
        // 更新用户信息并把更新后的完整用户信息返回给前端
        Users result = userService.updateUserInfo(user);

        return IMoocJSONResult.ok(result);

    }

    /**
     * 根据账号做匹配查询而不是模糊查询
     */
    @PostMapping("/search")
    public IMoocJSONResult searchUser(String myUserId, String friendUsername) throws Exception {

        // 判空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return IMoocJSONResult.errorMsg("");
        }

        // 1 搜索的用户不存在，"返回无此用户"
        // 2 搜索的是自己，返回"不能添加自己"
        // 3 搜索的已经是自己的好友，返回"该用户已经是你的好友"
        Integer searchResult = userService.preconditionSearchFriends(myUserId,friendUsername);

        if (searchResult == SearchFriendsStatusEnum.SUCCESS.status){
            Users user = userService.querUserInfobyName(friendUsername);
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(user,usersVO);
            return IMoocJSONResult.ok(usersVO);

        } else {
            String errormsg = SearchFriendsStatusEnum.getMsgByKey(searchResult);
            return IMoocJSONResult.errorMsg(errormsg);
        }

    }

    @PostMapping("/addFriendRequest")
    public IMoocJSONResult addFriendRequest(String myUserId, String friendUsername) throws Exception {

        // 判空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return IMoocJSONResult.errorMsg("");
        }

        // 1 搜索的用户不存在，"返回无此用户"
        // 2 搜索的是自己，返回"不能添加自己"
        // 3 搜索的已经是自己的好友，返回"该用户已经是你的好友"
        Integer searchResult = userService.preconditionSearchFriends(myUserId,friendUsername);

        if (searchResult == SearchFriendsStatusEnum.SUCCESS.status){

            userService.sendFriendRequest(myUserId, friendUsername);

        } else {
            String errormsg = SearchFriendsStatusEnum.getMsgByKey(searchResult);
            return IMoocJSONResult.errorMsg(errormsg);
        }

        return IMoocJSONResult.ok();

    }

}
