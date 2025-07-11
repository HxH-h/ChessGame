package com.game.Dao.Mapper;

import com.game.Controller.ControllerPojo.PlayerVO;
import com.game.Dao.Pojo.GameHistory;
import com.game.Dao.Pojo.Player;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PlayerMapper {
    @Insert("insert into historychess(first,second,time,vic,turn,chess,winner,loser) values (#{uuidA},#{uuidB},#{datetime},#{whoWin},#{turn},#{chess},#{winner},#{loser})")
    void saveHistory(String uuidA,String uuidB,String datetime,Integer whoWin,Integer turn,String chess,Integer winner,Integer loser);
}
