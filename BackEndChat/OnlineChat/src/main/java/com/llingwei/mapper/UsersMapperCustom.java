package com.llingwei.mapper;

import com.llingwei.pojo.Users;
import com.llingwei.pojo.vo.FriendRequestVO;
import com.llingwei.pojo.vo.MyFriendsVO;
import com.llingwei.utils.MyMapper;

import java.util.List;

public interface UsersMapperCustom extends MyMapper<Users> {

    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    public List<MyFriendsVO> queryMyFriends(String userId);

    public void updateMsgSigned (List<String> msgIdList);

}