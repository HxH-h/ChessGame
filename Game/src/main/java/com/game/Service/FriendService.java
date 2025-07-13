package com.game.Service;

import com.game.Dao.Pojo.Friend;
import com.game.Dao.Pojo.Player;
import com.game.Dao.Pojo.FriendDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 好友功能服务接口
 */
@Service
public interface FriendService {
    
    /**
     * 添加好友
     * @param friendUsername 好友用户名
     * @return 是否添加成功
     */
    boolean addFriend(String friendUsername);
    
    /**
     * 通过UUID添加好友
     * @param friendUuid 好友UUID
     * @return 是否添加成功
     */
    boolean addFriendByUuid(String friendUuid);
    
    /**
     * 删除好友
     * @param friendId 好友ID
     * @return 是否删除成功
     */
    boolean deleteFriend(String friendId);
    
    /**
     * 获取好友列表
     * @return 好友列表
     */
    List<Friend> getFriendList();
    
    /**
     * 查询同名用户
     * @param username 用户名
     * @return 同名用户列表
     */
    List<Player> findPlayersByUsername(String username);
    
    /**
     * 获取待处理的好友请求列表
     * @return 好友请求列表
     */
    List<Friend> getFriendRequests();
    
    /**
     * 接受好友请求
     * @param requestId 好友请求ID
     * @return 是否成功
     */
    boolean acceptFriendRequest(Integer requestId);
    
    /**
     * 拒绝好友请求
     * @param requestId 好友请求ID
     * @return 是否成功
     */
    boolean rejectFriendRequest(Integer requestId);

    /**
    * 获取包含玩家详情的好友列表
    * @return 包含玩家详情的好友列表
    */
    List<FriendDTO> getFriendListWithDetails();

    /**
    * 获取包含玩家详情的好友请求列表
    * @return 包含玩家详情的好友请求列表
    */
    List<FriendDTO> getFriendRequestsWithDetails();
}