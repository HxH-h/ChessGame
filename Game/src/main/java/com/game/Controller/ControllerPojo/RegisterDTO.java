package com.game.Controller.ControllerPojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String email;
    private String code;
}
