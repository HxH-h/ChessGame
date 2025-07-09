package com.game.Service;

import com.alibaba.fastjson.JSONObject;
import com.game.Controller.WebSocket;
import lombok.Data;

@Data
public class Gamer {
    String uuid;
    String username;

    public Gamer(String uuid , String username){
        this.uuid=uuid;
        this.username=username;
    }

    public void SendMessage(String event,Object message){
        JSONObject resp=new JSONObject();
        resp.put("event",event);
        resp.put(event,message);
        // 判断是否在线 在线则发送消息
        if (WebSocket.player.containsKey(uuid)){
            WebSocket.player.get(uuid).sendMessage(resp.toJSONString());
        }

    }
}
