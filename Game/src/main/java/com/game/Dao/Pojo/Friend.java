package com.game.Dao.Pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 好友关系实体类
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friend {
    private Integer id;
    private String user_uuid;  // 对应数据库中的 user_uuid
    private String friend_uuid;  // 对应数据库中的 friend_uuid
    private Integer status;  // 0=待确认, 1=已接受, 2=已拒绝, 3=已删除
    private String friend_username;  // 对应数据库中的 friend_username
    private String create_time;  // 对应数据库中的 create_time
    private String update_time;  // 对应数据库中的 update_time

    public String getFriendUuid() {
        return friend_uuid;
    }   

    public String getUserUuid() {
        return user_uuid;
    }

    public String getFriendUsername() {
        return friend_username;
    }
    
    public String getCreateTime() {
        return create_time;
    }
    
    public String getUpdateTime() {
        return update_time;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getId() {
        return id;
    }   


}