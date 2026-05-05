package com.officedubac.project.services;

import com.officedubac.project.dto.*;
import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.models.User;
import jakarta.mail.MessagingException;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

public interface AuthenticationService {

    User signup(SignUpDTO signUpDTO);

    User updatePassword(String email) throws MessagingException;

    String changedPassword(ChangedPasswordDTO changedPasswordDTO);

    JwtAuthenticationResponse signin(SignInDTO signInDTO) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException;

    JwtAuthenticationResponse refreshToken(RefreshTokenDTO refreshTokenDTO) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException;

    User getCurrentUser() throws BusinessResourceException;
}