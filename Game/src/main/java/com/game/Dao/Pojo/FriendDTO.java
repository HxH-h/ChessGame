package com.game.Dao.Pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 好友信息DTO，包含好友关系和好友的详细玩家信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendDTO {
    // 好友关系信息
    private Integer id;
    private String userUuid;        // 发起者UUID
    private String friendUuid;      // 接收者UUID
    private Integer status;         // 状态
    private String friendUsername;  // 对方用户名
    private String createTime;
    private String updateTime;
    
    // 好友详细信息（从Player表获取）
    private Integer level;
    private Integer score;
    private String photo;
    private String email;
    
    // 额外信息
    private String displayName;     // 显示名称
    private Boolean isRequester;    // 当前用户是否为请求发起方
}