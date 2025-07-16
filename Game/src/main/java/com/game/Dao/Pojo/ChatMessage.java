package com.game.Dao.Pojo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessage {
    String sender;
    String receiver;
    String message;
    String time;
}
