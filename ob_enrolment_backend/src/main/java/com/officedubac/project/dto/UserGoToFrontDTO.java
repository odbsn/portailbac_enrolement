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
public class UserGoToFrontDTO
{
    private String id;
    private String login;
    private String firstname;
    private String lastname;
    private boolean state_account;
    private boolean first_connexion;
    private String sessionId;
    private Profil profil;
    private Acteurs acteur;
}
