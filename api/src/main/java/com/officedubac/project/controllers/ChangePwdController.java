package com.officedubac.project.controllers;

import com.officedubac.project.dto.ChangedPasswordDTO;
import com.officedubac.project.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/security")
@RequiredArgsConstructor
@Tag(name="Password Controller", description = "Endpoints responsables de la gestion du mot de passe")
public class ChangePwdController
{
    @Autowired
    private final AuthenticationService authenticationService;
    @Operation(summary="Service de changement du mot de passe")
    @PutMapping("/changed-password")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> updatePassword(@RequestBody ChangedPasswordDTO changedPasswordDTO)
    {
        return ResponseEntity.ok(authenticationService.changedPassword(changedPasswordDTO));
    }
}
