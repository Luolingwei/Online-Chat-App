package com.imooc.service.Impl;

import com.imooc.enums.SearchFriendsStatusEnum;
import com.imooc.mapper.FriendsRequestMapper;
import com.imooc.mapper.MyFriendsMapper;
import com.imooc.mapper.UsersMapper;
import com.imooc.pojo.FriendsRequest;
import com.imooc.pojo.MyFriends;
import com.imooc.pojo.Users;
import com.imooc.service.UserService;
import com.imooc.utils.QRCodeUtils;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.io.File;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    private static final String FILE_SPACE = "/Users/luolingwei/Desktop/Program/OnlineChatApp/Online-Chat-App/UserFilesDB";

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

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


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUsername) {

        // 1 搜索的用户不存在，"返回无此用户"
        Users result = querUserInfobyName(friendUsername);
        if (result == null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }

        // 2 搜索的是自己，返回"不能添加自己"
        if (myUserId.equals(result.getId())){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }

        // 3 搜索的已经是自己的好友，返回"该用户已经是你的好友"
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
    public Users querUserInfobyName(String username){

        Example example = new Example(Users.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",username);
        Users result = usersMapper.selectOneByExample(example);
        return result;

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {

        // 根据用户名查询朋友信息
        Users friend = querUserInfobyName(friendUsername);

        // 1 查询是否已经存在对应的request
        Example example = new Example(FriendsRequest.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId",myUserId);
        criteria.andEqualTo("acceptUserId",friend.getId());
        FriendsRequest request = friendsRequestMapper.selectOneByExample(example);

        if (request==null){
            // 好友记录不存在，新增request, 已存在则不做任何操作
            FriendsRequest savedRequest = new FriendsRequest();
            savedRequest.setId(sid.nextShort());
            savedRequest.setSendUserId(myUserId);
            savedRequest.setAcceptUserId(friend.getId());
            savedRequest.setRequestDataTime(new Date());
            friendsRequestMapper.insert(savedRequest);
        }

    }
}
