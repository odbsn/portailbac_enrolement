package com.officedubac.project.dto;

import com.officedubac.project.models.Acteurs;
import com.officedubac.project.models.Profil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDTO {
    private String firstname;
    private String lastname;
    private String login;
    private String password;
    private String phone;
    private String email;
    private boolean state_account;
    private Profil profil;
    private Acteurs acteur;
}
