package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.IMoocJSONResult;
import com.imooc.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("u")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/RegistOrLogin")
    public IMoocJSONResult hello(@RequestBody Users user) throws Exception {

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


}
