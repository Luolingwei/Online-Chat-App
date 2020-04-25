package com.imooc.controller;

import com.imooc.enums.OperatorFriendRequestTypeEnum;
import com.imooc.enums.SearchFriendsStatusEnum;
import com.imooc.pojo.ChatMsg;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UsersBO;
import com.imooc.pojo.vo.MyFriendsVO;
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
import java.util.List;

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

    @PostMapping("/queryFriendRequests")
    public IMoocJSONResult queryFriendRequests(String userId) throws Exception {

        // 判空
        if (StringUtils.isBlank(userId) ){
            return IMoocJSONResult.errorMsg("");
        }

        // 查询用户接收到的朋友请求
        return IMoocJSONResult.ok(userService.queryFriendRequestList(userId));
    }

    // 接收方通过或者忽略朋友请求
    @PostMapping("/operFriendRequest")
    public IMoocJSONResult queryFriendRequests(String acceptUserId, String senderUserId, Integer operType) throws Exception {

        // 判空参数
        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(senderUserId) || operType==null){
            return IMoocJSONResult.errorMsg("");
        }

        // 如果operType没有对应的枚举值，抛错
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))){
            return IMoocJSONResult.errorMsg("");
        }

        if (operType.equals(OperatorFriendRequestTypeEnum.IGNORE.type)){
            // 忽略好友请求，直接删除request记录
            userService.deleteFriendRequest(acceptUserId,senderUserId);
        } else if (operType.equals(OperatorFriendRequestTypeEnum.PASS.type)){
            // 通过好友请求，删除request记录并互相保存好友
            userService.passFriendRequest(acceptUserId,senderUserId);
        }

        // 查询当前用户的好友列表 (获取最新的好友列表)
        List<MyFriendsVO> myFriendsVOList = userService.queryMyFriends(acceptUserId);

        return IMoocJSONResult.ok(myFriendsVOList);
    }

    // 查询当前用户的好友列表
    @PostMapping("/queryMyFriends")
    public IMoocJSONResult queryMyFriends(String userId) throws Exception {

        // 判空
        if (StringUtils.isBlank(userId) ){
            return IMoocJSONResult.errorMsg("");
        }

        // 查询当前用户的好友列表
        return IMoocJSONResult.ok(userService.queryMyFriends(userId));
    }

    // 用户手机端获取未签收的消息列表 (用户离线,没有channel, 没有在onmsg中获取)
    @PostMapping("/getUnReadMsgList")
    public IMoocJSONResult getUnReadMsgList(String acceptUserId) throws Exception {

        // 判空
        if (StringUtils.isBlank(acceptUserId) ){
            return IMoocJSONResult.errorMsg("");
        }

        // 获取未读消息列表
        List<ChatMsg> unReadMagList = userService.getUnReadMsgList(acceptUserId);

        return IMoocJSONResult.ok(unReadMagList);
    }

}
