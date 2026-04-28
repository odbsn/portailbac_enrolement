package com.officedubac.project.dto;

import lombok.Data;

@Data
public class SignUpDTO {
    private String usr_firstname;
    private String usr_lastname;
    private String usr_login;
    private String usr_password;
    private String phone;
    private String email;
    private boolean state_account;
    private String prfl_id;
}
