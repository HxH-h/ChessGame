package com.game.Controller;

import com.game.Constant.Code;
import com.game.Constant.Message;
import com.game.Controller.Response.Result;
import com.game.CusAnnotation.RequestLimit;
import com.game.Dao.Pojo.Friend;
import com.game.Dao.Pojo.Player;
import com.game.Dao.Pojo.FriendDTO;
import com.game.Service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 好友功能控制器
 */
@RestController
@Tag(name = "好友操作")
@RequestMapping("/api/friend")
@RequestLimit(interval = 10000, blockTime = 2)
public class FriendController {
    
    private static final Logger logger = LoggerFactory.getLogger(FriendController.class);

    @Autowired
    private FriendService friendService;

    /**
     * 查询同名用户
     */
    @GetMapping("/search")
    @Operation(summary = "查询同名用户")
    public Result<List<Player>> searchUsers(@RequestParam String username) {
        try {
            if (!StringUtils.hasText(username)) {
                return new Result<>(Code.FRIEND_NOT_EXIST, "用户名不能为空");
            }
            
            List<Player> players = friendService.findPlayersByUsername(username);
            if (players == null || players.isEmpty()) {
                return new Result<>(Code.FRIEND_NOT_EXIST, Message.FRIEND_NOT_EXIST);
            }
            return new Result<>(Code.GET_FRIEND_LIST_SUCCESS, "查询用户成功", players);
        } catch (Exception e) {
            logger.error("查询用户异常", e);
            return new Result<>(Code.REQUEST_BLOCK, "查询用户失败：" + e.getMessage());
        }
    }
    
    /**
     * 添加好友
     */
    @GetMapping("/add/{friendUsername}")
    @Operation(summary = "添加好友")
    public Result addFriend(@PathVariable String friendUsername) {
        try {
            if (!StringUtils.hasText(friendUsername)) {
                return new Result(Code.ADD_FRIEND_FAILURE, "好友用户名不能为空");
            }
            
            boolean success = friendService.addFriend(friendUsername);
            
            // 判断是否因为有多个同名用户导致的添加失败
            List<Player> players = friendService.findPlayersByUsername(friendUsername);
            if (!success && players != null && players.size() > 1) {
                // 有多个同名用户，返回特殊状态码和这些用户信息
                return new Result<>(Code.FRIEND_ALREADY_EXIST, "存在多个同名用户，请选择一个", players);
            }
            
            if (success) {
                return new Result(Code.ADD_FRIEND_SUCCESS, Message.ADD_FRIEND_SUCCESS);
            } else {
                return new Result(Code.ADD_FRIEND_FAILURE, Message.ADD_FRIEND_FAILURE);
            }
        } catch (Exception e) {
            logger.error("添加好友异常", e);
            return new Result(Code.ADD_FRIEND_FAILURE, "添加好友失败：" + e.getMessage());
        }
    }
    
    /**
     * 通过UUID添加好友
     */
    @PostMapping("/addByUuid")
    @Operation(summary = "通过UUID添加好友")
    public Result addFriendByUuid(@RequestParam String friendUuid) {
        try {
            if (!StringUtils.hasText(friendUuid)) {
                return new Result(Code.ADD_FRIEND_FAILURE, "好友UUID不能为空");
            }
            
            boolean success = friendService.addFriendByUuid(friendUuid);
            if (success) {
                return new Result(Code.ADD_FRIEND_SUCCESS, Message.ADD_FRIEND_SUCCESS);
            } else {
                return new Result(Code.ADD_FRIEND_FAILURE, Message.ADD_FRIEND_FAILURE);
            }
        } catch (Exception e) {
            logger.error("通过UUID添加好友异常", e);
            return new Result(Code.ADD_FRIEND_FAILURE, "添加好友失败：" + e.getMessage());
        }
    }

    /**
     * 删除好友
     */
    @GetMapping("/delete/{friendUuid}")
    @Operation(summary = "删除好友")
    public Result deleteFriend(@PathVariable String friendUuid) {
        try {
            if (!StringUtils.hasText(friendUuid)) {
                return new Result(Code.DELETE_FRIEND_FAILURE, "好友UUID不能为空");
            }
            
            boolean success = friendService.deleteFriend(friendUuid);
            if (success) {
                return new Result(Code.DELETE_FRIEND_SUCCESS, Message.DELETE_FRIEND_SUCCESS);
            } else {
                return new Result(Code.DELETE_FRIEND_FAILURE, Message.DELETE_FRIEND_FAILURE);
            }
        } catch (Exception e) {
            logger.error("删除好友异常", e);
            return new Result(Code.DELETE_FRIEND_FAILURE, "删除好友失败：" + e.getMessage());
        }
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取好友列表")
    public Result<List<Friend>> getFriendList() {
        try {
            List<Friend> friendList = friendService.getFriendList();
            return new Result<>(Code.GET_FRIEND_LIST_SUCCESS, Message.GET_FRIEND_LIST_SUCCESS, 
                    friendList != null ? friendList : Collections.emptyList());
        } catch (Exception e) {
            logger.error("获取好友列表异常", e);
            return new Result<>(Code.REQUEST_BLOCK, "获取好友列表失败：" + e.getMessage(), Collections.emptyList());
        }
    }
    
    /**
     * 获取好友请求列表
     */
    @GetMapping("/requests")
    @Operation(summary = "获取好友请求列表")
    public Result<List<Friend>> getFriendRequests() {
        try {
            List<Friend> friendRequests = friendService.getFriendRequests();
            return new Result<>(Code.GET_FRIEND_LIST_SUCCESS, "获取好友请求列表成功", 
                    friendRequests != null ? friendRequests : Collections.emptyList());
        } catch (Exception e) {
            logger.error("获取好友请求列表异常", e);
            return new Result<>(Code.REQUEST_BLOCK, "获取好友请求列表失败：" + e.getMessage(), Collections.emptyList());
        }
    }
    
    /**
     * 接受好友请求
     */
    @GetMapping("/accept/{requestId}")
    @Operation(summary = "接受好友请求")
    public Result acceptFriendRequest(@PathVariable Integer requestId) {
        try {
            if (requestId == null) {
                return new Result(Code.ADD_FRIEND_FAILURE, "请求ID不能为空");
            }
            
            boolean success = friendService.acceptFriendRequest(requestId);
            if (success) {
                return new Result(Code.ADD_FRIEND_SUCCESS, "接受好友请求成功");
            } else {
                return new Result(Code.ADD_FRIEND_FAILURE, "接受好友请求失败");
            }
        } catch (Exception e) {
            logger.error("接受好友请求异常", e);
            return new Result(Code.ADD_FRIEND_FAILURE, "接受好友请求失败：" + e.getMessage());
        }
    }
    
    /**
     * 拒绝好友请求
     */
    @GetMapping("/reject/{requestId}")
    @Operation(summary = "拒绝好友请求")
    public Result rejectFriendRequest(@PathVariable Integer requestId) {
        try {
            if (requestId == null) {
                return new Result(Code.ADD_FRIEND_FAILURE, "请求ID不能为空");
            }
            
            boolean success = friendService.rejectFriendRequest(requestId);
            if (success) {
                return new Result(Code.DELETE_FRIEND_SUCCESS, "拒绝好友请求成功");
            } else {
                return new Result(Code.DELETE_FRIEND_FAILURE, "拒绝好友请求失败");
            }
        } catch (Exception e) {
            logger.error("拒绝好友请求异常", e);
            return new Result(Code.DELETE_FRIEND_FAILURE, "拒绝好友请求失败：" + e.getMessage());
        }
    }

    /**
     * 获取包含玩家详情的好友列表
     */
    @GetMapping("/listWithDetails")
    @Operation(summary = "获取包含详细信息的好友列表")
    public Result<List<FriendDTO>> getFriendListWithDetails() {
        try {
            List<FriendDTO> friendList = friendService.getFriendListWithDetails();
            return new Result<>(Code.GET_FRIEND_LIST_SUCCESS, Message.GET_FRIEND_LIST_SUCCESS, 
                    friendList != null ? friendList : Collections.emptyList());
        } catch (Exception e) {
            logger.error("获取好友详情列表异常", e);
            return new Result<>(Code.REQUEST_BLOCK, "获取好友列表失败：" + e.getMessage(), Collections.emptyList());
        }
    }

    /**
     * 获取包含玩家详情的好友请求列表
     */
    @GetMapping("/requestsWithDetails")
    @Operation(summary = "获取包含详细信息的好友请求列表")
    public Result<List<FriendDTO>> getFriendRequestsWithDetails() {
        try {
            List<FriendDTO> requestList = friendService.getFriendRequestsWithDetails();
            return new Result<>(Code.GET_FRIEND_LIST_SUCCESS, "获取好友请求列表成功", 
                    requestList != null ? requestList : Collections.emptyList());
        } catch (Exception e) {
            logger.error("获取好友请求详情列表异常", e);
            return new Result<>(Code.REQUEST_BLOCK, "获取好友请求列表失败：" + e.getMessage(), Collections.emptyList());
        }
}
} 