package com.officedubac.project.controllers;

import com.officedubac.project.dto.*;
import com.officedubac.project.models.User;
import com.officedubac.project.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/authentification")
@RequiredArgsConstructor
@Tag(name="Auth Controller", description = "Endpoints responsables de l'authentification")
public class AuthController
{
    @Autowired
    private final AuthenticationService authenticationService;

    @Operation(summary="Connexion de n'importe quel utilisateur (login, password)")
    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SignInDTO signInDTO) throws Exception {
        return ResponseEntity.ok(authenticationService.signin(signInDTO));
    }

    @Operation(summary="Rafraîchisseur du token de sécurité programmé à chaque 15 minutes")
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtAuthenticationResponse> refresh_token(@RequestBody RefreshTokenDTO refreshTokenDTO) throws Exception {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenDTO));
    }

    @Operation(summary="Service de mis à jour du mot de passe pour les externes")
    @PutMapping("/update-password-for-public")
    public ResponseEntity<?> updatePassword(@RequestParam String email) throws MessagingException {
        User updated = authenticationService.updatePassword(email);

        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Utilisateur introuvable pour l’email : " + email));
        }

        return ResponseEntity.ok(updated);
    }

}