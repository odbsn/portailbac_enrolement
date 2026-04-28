package com.officedubac.project.dto;

import lombok.Data;

@Data
public class ChangedPasswordDTO
{
    private String usr_password;
    private String new_password;
}
