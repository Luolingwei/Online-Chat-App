package com.llingwei.controller;

import com.llingwei.enums.OperatorFriendRequestTypeEnum;
import com.llingwei.enums.SearchFriendsStatusEnum;
import com.llingwei.pojo.ChatMsg;
import com.llingwei.pojo.Users;
import com.llingwei.pojo.bo.UsersBO;
import com.llingwei.pojo.vo.MyFriendsVO;
import com.llingwei.pojo.vo.UsersVO;
import com.llingwei.service.UserService;
import com.llingwei.utils.FileUtils;
import com.llingwei.utils.JSONResult;
import com.llingwei.utils.MD5Utils;
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

    @PostMapping("/RegisterLogin")
    public JSONResult RegisterLogin(@RequestBody Users user) throws Exception {

        // 0 None judgement
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())){
            return JSONResult.errorMsg("Username or Password can NOT be empty...");
        }

        // 1 Judge whether username exist, if exist - login, if not exist - register
        boolean usernameIsExist = userService.queryUsernameExist(user.getUsername());

        Users userResult = null;
        if (usernameIsExist){
            // 1.1 login
            userResult = userService.queryUserLoginSuccess(user.getUsername(),MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null){
                return JSONResult.errorMsg("Username or Password Incorrect...");
            }
            userResult = userService.querUserbyName(user.getUsername());
            userResult.setPublicKey(user.getPublicKey());
            userService.updateUser(userResult);

        } else {
            // 1.2 register
//            RSAController crypto = new RSAController();
//            KeyPair keyPair = crypto.generateKey();
//            PublicKey publicKey = keyPair.getPublic();
//            PrivateKey privateKey = keyPair.getPrivate();
//
//            String pubKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
//            String priKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());

            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult = userService.saveUser(user);
        }

        // filter out unnecessary fields，then return to frontend
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        return JSONResult.ok(usersVO);
    }

    @PostMapping("/updateNickname")
    public JSONResult updateNickname(@RequestBody UsersBO usersBO) throws Exception {

        // update user info in DB
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setNickname(usersBO.getNickName());

        // return updated user info to frontend
        Users result = userService.updateUser(user);

        return JSONResult.ok(result);

    }

    @PostMapping("/uploadFaceImage")
    public JSONResult uploadFaceBase64(@RequestBody UsersBO usersBO) throws Exception {

        // obtain base64 string from frontend and store it into local DB
        String base64Data = usersBO.getFaceData();
        String uploadPathDB = "/" + usersBO.getUserId() + "/face" + "/userface64.png";
        String finalPath = FILE_SPACE + uploadPathDB;
        String dirPath = FILE_SPACE + "/" + usersBO.getUserId() + "/face";

        System.out.println(uploadPathDB);
        System.out.println(finalPath);

        File file = new File(dirPath);
        file.mkdirs();

        // transfer base64 string to file
        FileUtils.base64ToFile(finalPath,base64Data);

        // update user info in DB
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setFaceImage(uploadPathDB);
        user.setFaceImageBig(uploadPathDB);

        // return updated user info to frontend
        Users result = userService.updateUser(user);

        return JSONResult.ok(result);

    }

    @PostMapping("/searchUser")
    public JSONResult searchUser(String myUserId, String friendUsername) throws Exception {

        // None judgement
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return JSONResult.errorMsg("");
        }


        Integer searchResult = userService.SearchFriendsPreCheck(myUserId,friendUsername);

            // 0 success
        if (searchResult == SearchFriendsStatusEnum.SUCCESS.status){
            Users user = userService.querUserbyName(friendUsername);
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(user,usersVO);
            return JSONResult.ok(usersVO);

        } else {
            // 1 user searched doesn't exist，return ""No such user"
            // 2 search himself，return "You can not add yourself..."
            // 3 search someone in his friend list，return "This user is already your friend..."
            String errormsg = SearchFriendsStatusEnum.getMsgByKey(searchResult);
            return JSONResult.errorMsg(errormsg);
        }

    }

    @PostMapping("/addFriend")
    public JSONResult addFriendRequest(String myUserId, String friendUsername) throws Exception {

        // None judgement
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return JSONResult.errorMsg("");
        }

        Integer searchResult = userService.SearchFriendsPreCheck(myUserId,friendUsername);

            // 0 success
        if (searchResult == SearchFriendsStatusEnum.SUCCESS.status){

            userService.sendFriendRequest(myUserId, friendUsername);

        } else {
            // 1 user searched doesn't exist，return ""No such user"
            // 2 search himself，return "You can not add yourself..."
            // 3 search someone in his friend list，return "This user is already your friend..."
            String errormsg = SearchFriendsStatusEnum.getMsgByKey(searchResult);
            return JSONResult.errorMsg(errormsg);
        }

        return JSONResult.ok();

    }

    @PostMapping("/searchFriendRequests")
    public JSONResult searchFriendRequests(String userId) throws Exception {

        // None judgement
        if (StringUtils.isBlank(userId) ){
            return JSONResult.errorMsg("");
        }

        // Query this user's friend requests
        return JSONResult.ok(userService.queryFriendRequestList(userId));
    }

    // receiver can accept/ignore friend request
    @PostMapping("/handleFriendRequest")
    public JSONResult queryFriendRequests(String acceptUserId, String senderUserId, Integer operType) throws Exception {

        // None judgement
        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(senderUserId) || operType==null){
            return JSONResult.errorMsg("");
        }

        // if operType is not 0 or 1, return error
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))){
            return JSONResult.errorMsg("");
        }

        if (operType.equals(OperatorFriendRequestTypeEnum.IGNORE.type)){
            // friend request is ignored，delete request in DB
            userService.deleteFriendRequest(acceptUserId,senderUserId);
        } else if (operType.equals(OperatorFriendRequestTypeEnum.PASS.type)){
            // friend request is accepted，delete request in DB, then save friends relationship
            userService.acceptFriendRequest(acceptUserId,senderUserId);
        }

        // query current user's friend list (obtain updated friend list)
        List<MyFriendsVO> myFriendsVOList = userService.queryFriendList(acceptUserId);

        return JSONResult.ok(myFriendsVOList);
    }

    // query current user's friend list
    @PostMapping("/searchMyFriends")
    public JSONResult queryMyFriends(String userId) throws Exception {

        // None judgement
        if (StringUtils.isBlank(userId) ){
            return JSONResult.errorMsg("");
        }

        // query current user's friend list
        return JSONResult.ok(userService.queryFriendList(userId));
    }

    // obtain unread msg list from phone
    @PostMapping("/searchUnReadMsgList")
    public JSONResult getUnReadMsgList(String acceptUserId) throws Exception {

        // None judgement
        if (StringUtils.isBlank(acceptUserId) ){
            return JSONResult.errorMsg("");
        }

        // obtain unread msg list
        List<ChatMsg> unReadMagList = userService.getUnSignedMsgList(acceptUserId);

        return JSONResult.ok(unReadMagList);
    }

    // obtain user's publicKey by user's id
    @PostMapping("/getUserPublicKey")
    public JSONResult getUserPublicKey(String userId) throws Exception {

        // None judgement
        if (StringUtils.isBlank(userId) ){
            return JSONResult.errorMsg("");
        }

        // obtain user's public key
        String publicKey = userService.searchPublicKey(userId);

        return JSONResult.ok(publicKey);
    }

}
