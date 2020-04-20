package com.imooc.service.Impl;

import com.imooc.mapper.UsersMapper;
import com.imooc.pojo.Users;
import com.imooc.service.UserService;
import com.imooc.utils.QRCodeUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.io.File;

@Service
public class UserServiceImpl implements UserService {

    private static final String FILE_SPACE = "/Users/luolingwei/Desktop/Program/OnlineChatApp/Online-Chat-App/UserFilesDB";

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {

        Users user = new Users();
        user.setUsername(username);
        Users result = usersMapper.selectOne(user);
        return result != null;

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String password) {


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

        // 为每个用户生成唯一的二维码

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
    public Users updateUserInfo(Users user) {

        usersMapper.updateByPrimaryKeySelective(user);
        return queryUserById(user.getId());

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    private Users queryUserById(String userId){
        return usersMapper.selectByPrimaryKey(userId);
    }
}
