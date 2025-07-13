package com.game.Service.Impl;

import com.game.Controller.UserInfoThread;
import com.game.Dao.Mapper.FriendMapper;
import com.game.Dao.Mapper.PlayerMapper;
import com.game.Dao.Pojo.Friend;
import com.game.Dao.Pojo.Player;
import com.game.Dao.Pojo.FriendDTO;
import com.game.Service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;



/**
 * 好友功能服务实现类
 */
@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private PlayerMapper playerMapper;


    @Override
    public List<FriendDTO> getFriendListWithDetails() {
        String currentUserUuid = UserInfoThread.getInfo();
        if (!StringUtils.hasText(currentUserUuid)) {
            return Collections.emptyList(); // 用户未登录，返回空列表
        }
        try {
            // 获取已接受的好友关系列表（状态为1）
            List<Friend> friendList = friendMapper.getFriendList(currentUserUuid);
            List<FriendDTO> friendDTOList = new ArrayList<>();
            
            for (Friend friend : friendList) {
                // 确定当前用户是发送方还是接收方
                boolean isRequester = currentUserUuid.equals(friend.getUserUuid());
                
                // 确定对方的UUID
                String otherUserUuid = isRequester ? 
                    friend.getFriendUuid() : friend.getUserUuid();
                
                // 查询对方的玩家信息
                Player otherPlayer = playerMapper.selectByUUID(otherUserUuid);
                if (otherPlayer == null) {
                    continue; // 如果找不到玩家信息，跳过
                }
                
                // 创建并填充DTO
                FriendDTO dto = new FriendDTO();
                dto.setId(friend.getId());
                dto.setUserUuid(friend.getUserUuid());
                dto.setFriendUuid(friend.getFriendUuid());
                dto.setStatus(friend.getStatus());
                dto.setCreateTime(friend.getCreateTime());
                dto.setUpdateTime(friend.getUpdateTime());
                
                // 设置显示名称和其他玩家信息
                dto.setFriendUsername(otherPlayer.getUsername());
                dto.setDisplayName(otherPlayer.getUsername());
                dto.setLevel(otherPlayer.getLevel());
                dto.setScore(otherPlayer.getScore());
                dto.setPhoto(otherPlayer.getPhoto());
                dto.setEmail(otherPlayer.getEmail());
                dto.setIsRequester(isRequester);
                
                friendDTOList.add(dto);
            }
            
            return friendDTOList;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<FriendDTO> getFriendRequestsWithDetails() {
        String currentUserUuid = UserInfoThread.getInfo();
        if (!StringUtils.hasText(currentUserUuid)) {
            return Collections.emptyList(); // 用户未登录
        }
        
        try {
            // 获取发送给当前用户的待确认好友请求（状态为0）
            List<Friend> requests = friendMapper.getFriendRequests(currentUserUuid);
            List<FriendDTO> requestDTOList = new ArrayList<>();
            
            for (Friend request : requests) {
                // 请求发送方的UUID
                String requesterUuid = request.getUserUuid();
                
                // 查询发送方的玩家信息
                Player requester = playerMapper.selectByUUID(requesterUuid);
                if (requester == null) {
                    continue; // 如果找不到玩家信息，跳过
                }
                
                // 创建并填充DTO
                FriendDTO dto = new FriendDTO();
                dto.setId(request.getId());
                dto.setUserUuid(request.getUserUuid());
                dto.setFriendUuid(request.getFriendUuid());
                dto.setStatus(request.getStatus());
                dto.setCreateTime(request.getCreateTime());
                dto.setUpdateTime(request.getUpdateTime());
                
                // 设置显示名称和其他玩家信息
                dto.setFriendUsername(requester.getUsername());
                dto.setDisplayName(requester.getUsername());
                dto.setLevel(requester.getLevel());
                dto.setScore(requester.getScore());
                dto.setPhoto(requester.getPhoto());
                dto.setEmail(requester.getEmail());
                dto.setIsRequester(false); // 当前用户不是请求发送方
                
                requestDTOList.add(dto);
            }
            
            return requestDTOList;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
public boolean addFriend(String friendUsername) {
    // 参数验证
    if (!StringUtils.hasText(friendUsername)) {
        return false;
    }
    
    String userUuid = UserInfoThread.getInfo();
    if (!StringUtils.hasText(userUuid)) {
        return false; // 用户未登录
    }
    
    // 获取当前用户的用户名，不能添加自己为好友
    Player currentUser = playerMapper.selectByUUID(userUuid);
    if (currentUser != null && friendUsername.equals(currentUser.getUsername())) {
        return false; // 不能添加自己为好友
    }
    
    // 查询用户，如果只有一个则直接添加
    List<Player> players = friendMapper.findPlayersByUsername(friendUsername);
    if (players == null || players.isEmpty()) {
        return false; // 好友不存在
    }
    
    if (players.size() == 1) {
        // 只有一个用户，直接处理
        Player friend = players.get(0);
        
        try {
            // 检查是否已经是好友或已发送请求
            int count = friendMapper.checkFriendExists(userUuid, friend.getUuid());
            if (count > 0) {
                return false; // 已经是好友或已发送请求
            }
            
            // 当前时间
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = formatter.format(new Date(System.currentTimeMillis()));
            
            // 添加好友关系 (状态为0表示待确认)
            Friend friendRelation = Friend.builder()
                    .user_uuid(userUuid)
                    .friend_uuid(friend.getUuid())
                    .status(0) // 待确认
                    .friend_username(friend.getUsername())
                    .create_time(currentTime)
                    .update_time(currentTime)
                    .build();
                
            friendMapper.addFriend(friendRelation);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 有多个同名用户，返回false，由控制器处理
    return false;
}
    
    
    @Override
    public boolean addFriendByUuid(String friendUuid) {
        return false;
    }

    @Override
    public boolean deleteFriend(String friendUuid) {
        // 参数验证
        if (!StringUtils.hasText(friendUuid)) {
            return false;
        }
        
        String userUuid = UserInfoThread.getInfo();
        if (!StringUtils.hasText(userUuid)) {
            return false; // 用户未登录
        }
        
        // 检查是否存在好友关系
        int count = friendMapper.checkFriendExists(userUuid, friendUuid);
        if (count == 0) {
            return false;
        }
        
        try {
            // 删除好友关系
            friendMapper.deleteFriend(userUuid, friendUuid);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Friend> getFriendList() {
        String userUuid = UserInfoThread.getInfo();
        if (!StringUtils.hasText(userUuid)) {
            return Collections.emptyList(); // 用户未登录，返回空列表
        }
        
        try {
            return friendMapper.getFriendList(userUuid);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Player> findPlayersByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return Collections.emptyList();
        }
        
        try {
            return friendMapper.findPlayersByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Friend> getFriendRequests() {
        String userUuid = UserInfoThread.getInfo();
        if (!StringUtils.hasText(userUuid)) {
            return Collections.emptyList(); // 用户未登录
        }
        
        try {
            return friendMapper.getFriendRequests(userUuid);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean acceptFriendRequest(Integer requestId) {
        if (requestId == null) {
            System.out.println("请求ID为空");

            return false;
        }
        
        String userUuid = UserInfoThread.getInfo();
        if (!StringUtils.hasText(userUuid)) {
            System.out.println("用户未登录");
            return false; // 用户未登录
        }
        
        try {
            // 获取好友请求
            Friend request = friendMapper.getFriendRequestById(requestId);
            if (request == null) {
                System.out.println("好友请求不存在");
                return false;
            }
            
            // 确认请求是发给当前用户的
            if (!userUuid.equals(request.getFriendUuid())) {
                System.out.println(userUuid);
                System.out.println(request.getFriendUuid());
                System.out.println("好友请求不是发给当前用户的");
                return false;
            }
            
            // 当前时间
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = formatter.format(new Date(System.currentTimeMillis()));
            
            // 更新为已接受
            friendMapper.updateFriendRequestStatus(requestId, 1, currentTime);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean rejectFriendRequest(Integer requestId) {
        if (requestId == null) {
            return false;
        }
        
        String userUuid = UserInfoThread.getInfo();
        if (!StringUtils.hasText(userUuid)) {
            return false; // 用户未登录
        }
        
        try {
            // 获取好友请求
            Friend request = friendMapper.getFriendRequestById(requestId);
            if (request == null) {
                return false;
            }
            
            // 确认请求是发给当前用户的
            if (!userUuid.equals(request.getFriendUuid())) {
                return false;
            }
            
            // 直接删除这条好友请求记录
            friendMapper.deleteFriendById(requestId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 