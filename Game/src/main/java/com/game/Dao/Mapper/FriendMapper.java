package com.game.Dao.Mapper;

import com.game.Dao.Pojo.Friend;
import com.game.Dao.Pojo.Player;
import com.game.Dao.Pojo.FriendDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FriendMapper {
    
    /**
     * 添加好友关系
     */
    @Insert("INSERT INTO friend(user_uuid, friend_uuid, status, friend_username, create_time, update_time) " +
            "VALUES(#{userUuid}, #{friendUuid}, #{status}, #{friendUsername}, #{createTime}, #{updateTime})")
    void addFriend(Friend friend);
    
    /**
     * 删除好友关系 (双向删除，无论是发起方还是接收方)
     */
    @Delete("DELETE FROM friend WHERE (user_uuid = #{userUuid} AND friend_uuid = #{friendUuid}) OR (user_uuid = #{friendUuid} AND friend_uuid = #{userUuid})")
    void deleteFriend(String userUuid, String friendUuid);
    
    /**
     * 获取好友列表 (已接受的好友)
     */
    @Select("SELECT id, user_uuid, friend_uuid, status, " +
            "friend_username, create_time, update_time " +
            "FROM friend WHERE (user_uuid = #{userUuid} OR friend_uuid = #{userUuid}) AND status = 1")
    List<Friend> getFriendList(String userUuid);
    
    /**
     * 检查好友关系是否存在
     */
    @Select("SELECT COUNT(*) FROM friend WHERE (user_uuid = #{userUuid} AND friend_uuid = #{friendUuid} OR user_uuid = #{friendUuid} AND friend_uuid = #{userUuid}) AND (status = 0 OR status = 1)")
    int checkFriendExists(String userUuid, String friendUuid);
    
    /**
     * 根据用户名查询用户UUID
     */
    @Select("SELECT uuid FROM player WHERE username = #{username}")
    String getUserUuidByUsername(String username);
    
    /**
     * 根据UUID获取用户名
     */
    @Select("SELECT username FROM player WHERE uuid = #{uuid}")
    String getUsernameByUuid(String uuid);
    
    /**
     * 查询同名用户列表
     */
    @Select("SELECT uuid, username, email, level, score FROM player WHERE username = #{username}")
    List<Player> findPlayersByUsername(String username);
    
    /**
     * 获取待处理的好友请求列表
     */
    @Select("SELECT f.id, f.user_uuid, f.friend_uuid, f.status, " +
            "f.friend_username, f.create_time, f.update_time, " +
            "p.username as senderUsername " +
            "FROM friend f JOIN player p ON f.user_uuid = p.uuid " +
            "WHERE f.friend_uuid = #{userUuid} AND f.status = 0")
    List<Friend> getFriendRequests(String userUuid);
    
    /**
     * 根据ID查询好友请求
     */
    @Select("SELECT * FROM friend WHERE id = #{id}")
    Friend getFriendRequestById(Integer id);
    
    /**
     * 更新好友请求状态
     */
    @Update("UPDATE friend SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    void updateFriendRequestStatus(Integer id, Integer status, String updateTime);

    /**
     * 获取包含玩家详情的好友列表
     */
    @Select("SELECT f.*, p.username, p.email, p.level, p.score, p.photo FROM friend f JOIN player p ON f.friend_uuid = p.uuid WHERE (f.user_uuid = #{userUuid} OR f.friend_uuid = #{userUuid}) AND f.status = 1")
    List<FriendDTO> getFriendListWithDetails(String userUuid);
    
    /**
     * 根据ID删除好友关系
     */
    @Delete("DELETE FROM friend WHERE id = #{id}")
    void deleteFriendById(Integer id);
} 