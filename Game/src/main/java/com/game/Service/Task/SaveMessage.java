package com.game.Service.Task;

import com.game.Constant.RedisConstant;
import com.game.Dao.Mapper.PlayerMapper;
import com.game.Dao.Pojo.ChatMessage;
import com.game.Service.Impl.FriendServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

// 定时持久化消息
@Slf4j
@Component

public class SaveMessage {

    @Autowired
    PlayerMapper playerMapper;

    // 操作redis
    public static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        SaveMessage.redisTemplate = redisTemplate;
    }

    public Set<String> getAllChatKeys() {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(RedisConstant.chat + "*").build();

        Cursor<byte[]> cursor = (Cursor<byte[]>) redisTemplate.execute((RedisCallback<Cursor<byte[]>>) connection -> {
            return connection.scan(options);
        });

        while (cursor != null && cursor.hasNext()) {
            byte[] keyBytes = cursor.next();
            String key = new String(keyBytes);
            System.out.println(key);
            keys.add(key);
        }

        return keys;
    }

    public ChatMessage convertToChatMessage(HashMap<String, Object> data, String userA, String userB) {
        String message = (String) data.get("message");
        String time = (String) data.get("time");
        int direction = (Integer) data.get("direction");

        String actualSender = direction == 0 ? userA : userB;
        String actualReceiver = direction == 0 ? userB : userA;

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(actualSender)
                .receiver(actualReceiver)
                .message(message)
                .time(time)
                .build();
        return chatMessage;
    }

    //@Scheduled(fixedRate = 3600000)
    @Scheduled(fixedDelay = 60000)
    public void saveMessage() {
        // 封装为ChatMessage
        List<ChatMessage> result = new ArrayList<>();
        Set<String> keys = getAllChatKeys();

        for (String key : keys) {

            String[] split = key.split(":");
            String userA = split[2];
            String userB = split[3];
            Set<HashMap<String, Object>> messages = redisTemplate.opsForZSet().range(key, 0, -1);
            if (messages != null){
                for (HashMap<String, Object> msg : messages) {
                    ChatMessage chatMessage = convertToChatMessage(msg, userA, userB);
                    result.add(chatMessage);
                }
            }
            redisTemplate.delete(key);
        }

        // 存储到数据库
        if (!result.isEmpty()) playerMapper.saveMessage(result);
        log.info("保存了{}条消息", result.size());
    }
}
