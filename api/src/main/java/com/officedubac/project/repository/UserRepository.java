package com.officedubac.project.repository;

import com.officedubac.project.models.Profil;
import com.officedubac.project.models.Role;
import com.officedubac.project.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String>
{
    Optional<User> findByLogin(String login);
    User findByEmail(String email);

    List<User> findByActeur_EtablissementId(String idEtab);

    List<User> findByProfil_Name(Role name);
}
