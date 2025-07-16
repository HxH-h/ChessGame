package com.game.Controller.ControllerPojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageVO {
   String message;
   String time;
   boolean isme;
}
