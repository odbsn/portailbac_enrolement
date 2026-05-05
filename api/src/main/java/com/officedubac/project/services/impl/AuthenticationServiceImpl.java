package com.officedubac.project.services.impl;

import com.officedubac.project.dto.*;
import com.officedubac.project.models.*;
import com.officedubac.project.repository.ProfilRepository;
import com.officedubac.project.repository.UserRepository;
import com.officedubac.project.services.AuthenticationService;
import com.officedubac.project.services.EmailService;
import com.officedubac.project.services.JWTService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final EmailService emailService;

    @Autowired
    private final ProfilRepository profilRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    public User signup(SignUpDTO signUpDTO)
    {
        logger.info("TEST : " + signUpDTO.getUsr_password());
        Profil profil = this.profilRepository.findById(signUpDTO.getPrfl_id()).orElse(null);
        User user = new User();
        user.setFirstname(signUpDTO.getUsr_firstname());
        user.setLastname(signUpDTO.getUsr_lastname());
        user.setLogin(signUpDTO.getUsr_login());
        user.setPassword(passwordEncoder.encode(signUpDTO.getUsr_password()));
        user.setPhone(signUpDTO.getPhone());
        user.setEmail(signUpDTO.getEmail());
        user.setProfil(profil);

        return userRepository.save(user);

    }

    //A la premiere connexion
    public String changedPassword(ChangedPasswordDTO changedPasswordDTO)
    {
        // Retrieve the existing Utilisateur from the repository
        //User existingUser = userRepository.findById(usrId).orElse(null);
        User existingUser = getCurrentUser();
        boolean matches = passwordEncoder.matches(changedPasswordDTO.getUsr_password(), existingUser.getPassword());

        if (matches)
        {
            System.out.println(changedPasswordDTO.getNew_password());
            // Update the fields of the existing Utilisateur
            existingUser.setPassword(passwordEncoder.encode(changedPasswordDTO.getNew_password()));
            existingUser.setFirst_connexion(false);
            userRepository.save(existingUser);
            return "Le mot de passe a été mis à jour avec succés.";
        }
        else
        {
            return "Le mot de passe d’origine est incorrect.";
        }

    }

    //Mot de passe perdu
    public User updatePassword(String email) throws MessagingException {
        // Retrieve the existing Utilisateur from the repository
        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null)
        {
            String newPassword = CodeGenerator.generateCode();
            // Update the fields of the existing Utilisateur
            existingUser.setPassword(passwordEncoder.encode(newPassword));
            existingUser.setFirst_connexion(true);
            Map<String, Object> variables = new HashMap<>();
            variables.put("title", "Bienvenue sur PortailBAC \uD83C\uDF89 !");
            variables.put("message", "Votre mot de passe a été réinitialisé avec succés.");
            variables.put("login", existingUser.getLogin());
            variables.put("password", newPassword);

            emailService.sendEmailPasswordReinitialised(existingUser.getEmail(), "[Office du Baccalauréat / PortailBAC] Réinitialisation du mot de passe", variables);
            return userRepository.save(existingUser);
        }
        else
        {
            return null;
        }
    }

    public JwtAuthenticationResponse signin(SignInDTO signInDTO) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        try {
            logger.info("Start Authentification");
            //logger.info("Login: " + signInDTO.getLogin());
            //Mot de passe à cacher aprés sur les log
            //logger.info("Password: " + (signInDTO.getPassword() != null ? signInDTO.getPassword() : "null"));

            // Authentification de l'utilisateur
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInDTO.getLogin(), signInDTO.getPassword()));
            //logger.info("Etape A");

            // Recherche de l'utilisateur dans la base de données
            var user = userRepository.findByLogin(signInDTO.getLogin()).orElseThrow(
                    () -> new IllegalArgumentException("Invalid login or password"));
            //logger.info("Etape B");

            UserGoToFrontDTO usergoToFront = new UserGoToFrontDTO();
            User users = userRepository.findById(user.getId()).orElse(null);

            usergoToFront.setLogin(users.getLogin());
            usergoToFront.setFirstname(users.getFirstname());
            usergoToFront.setLastname(users.getLastname());
            usergoToFront.setActeur(users.getActeur());
            usergoToFront.setState_account(users.isState_account());
            usergoToFront.setFirst_connexion(users.isFirst_connexion());
            usergoToFront.setId(users.getId());

            Profil profils = profilRepository.findById(users.getProfil().getId()).orElse(null);
            usergoToFront.setProfil(profils);

            // Génération du token JWT
            var jwt = jwtService.generateToken(user);
            //logger.info("JWT: " + jwt);

            // Génération du refresh token
            var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
            //logger.info("Refresh Token: " + refreshToken);

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setToken(jwt);
            jwtAuthenticationResponse.setRefreshToken(refreshToken);
            jwtAuthenticationResponse.setUser(usergoToFront);

            //logger.info("Response generated.");
            return jwtAuthenticationResponse;
        }
        catch (BadCredentialsException e)
        {
            // Cas classique : mauvais identifiants
            logger.warn("Tentative d'authentification échouée pour l'utilisateur.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides.");
        }
        catch (IllegalArgumentException e)
        {
            // Cas de login non trouvé
            //logger.warn("Erreur d'identification : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides.");
        }
        catch (Exception e) {
            // Cas générique — sans exposer les détails
            //logger.error("Erreur interne lors du processus de connexion: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur.");
        }
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenDTO refreshTokenDTO) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        try
        {
            logger.info("Start");
            //logger.info(refreshTokenDTO.getToken());
            String login = jwtService.extractUserName(refreshTokenDTO.getToken());
            //logger.info(login);
            User user = userRepository.findByLogin(login).orElseThrow();
            if(jwtService.isTokenValid(refreshTokenDTO.getToken(), user))
            {
                var jwt = jwtService.generateToken(user);
                JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();

                jwtAuthenticationResponse.setToken(jwt);
                jwtAuthenticationResponse.setRefreshToken(refreshTokenDTO.getToken());
                return jwtAuthenticationResponse;
            }
            return null;
        }
        catch (Exception e)
        {
            //logger.error("Error during sign-in process: ", e);
            throw e;  // Rethrow or handle the exception as needed
        }
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // Votre classe User implémente UserDetails, donc le principal est directement l'objet User
        if (principal instanceof User) {
            return (User) principal;
        }

        // Fallback
        String login = authentication.getName();
        if (login != null && !login.isEmpty()) {
            return userRepository.findByLogin(login).orElse(null);
        }
        return null;
    }

}