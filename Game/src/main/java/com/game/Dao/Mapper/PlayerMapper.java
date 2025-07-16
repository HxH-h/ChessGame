package com.game.Dao.Mapper;

import com.game.Controller.ControllerPojo.FriendVO;
import com.game.Controller.ControllerPojo.MessageVO;
import com.game.Controller.ControllerPojo.PlayerVO;
import com.game.Dao.Pojo.ChatMessage;
import com.game.Dao.Pojo.GameHistory;
import com.game.Dao.Pojo.Player;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PlayerMapper {

  
    List<FriendVO> getFriends(String uuid);

    List<MessageVO> getMessage(String userA, String userB , int limit);

    void saveMessage(List<ChatMessage> messages);
}
