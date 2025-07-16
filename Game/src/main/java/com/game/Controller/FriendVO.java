package com.game.Controller.ControllerPojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendVO {
    String uuid;
    String username;
    String photo;
    String status;
}
