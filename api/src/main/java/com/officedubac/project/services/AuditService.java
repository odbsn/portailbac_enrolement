package com.officedubac.project.services;

import com.officedubac.project.dto.CandidatDTO;
import com.officedubac.project.dto.CandidatDecisionDTO;
import com.officedubac.project.models.AuditLog;
import com.officedubac.project.models.Candidat;
import com.officedubac.project.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    @Autowired
    private final AuditLogRepository auditRepo;

    public void logOperation(String entityName,
                             String entityId,
                             String operationType,
                             Map<String, Object> oldValues,
                             String login,
                             String ipAddress) {
        AuditLog log = new AuditLog();
        log.setNatureOperation(entityName);
        log.setIdCandidate(entityId);
        log.setOperationType(operationType);
        log.setFieldValues(oldValues);
        log.setLogin(login);
        log.setIpAddress(ipAddress);

        auditRepo.save(log);
    }

    private String decisionLabel(Integer value) {
        if (value == null) return null;
        return switch (value) {
            case 1 -> "Dossier Accepté par OB";
            case 2 -> "Dossier Rejeté par OB";
            default -> value.toString();
        };
    }

    public List<AuditLog> getLogsByCandidateId(String candidateId) {
        return auditRepo.findByIdCandidate(candidateId);
    }

    public Map<String, Object> getDifferences(Candidat oldC, CandidatDecisionDTO newC) {
        Map<String, Object> differences = new LinkedHashMap<>();

        String dateNaissance = oldC.getDate_birth()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Helper lambda pour simplifier l'écriture
        BiFunction<Object, Object, Map<String, String>> diff = (o, n) ->
                Map.ofEntries(
                        Map.entry("ancienne-donnée", o == null ? "null" : Objects.toString(o, "null")),
                        Map.entry("nouvelle-donnée", n == null ? "null" : Objects.toString(n, "null"))
                );

        if (!Objects.equals(oldC.getDosNumber(), newC.getDosNumber())) {
            differences.put("N° de dossier",
                    diff.apply(oldC.getDosNumber(), newC.getDosNumber()));
        }

        if (!Objects.equals(oldC.getFirstname(), newC.getFirstname())) {
            differences.put("Prénom (s)",
                    diff.apply(oldC.getFirstname(), newC.getFirstname()));
        }

        if (!Objects.equals(oldC.getLastname(), newC.getLastname())) {
            differences.put("Nom",
                    diff.apply(oldC.getLastname(), newC.getLastname()));
        }

        if (!Objects.equals(dateNaissance, newC.getDate_birth())) {
            differences.put("Date de naissance",
                    diff.apply(dateNaissance, newC.getDate_birth()));
        }

        if (!Objects.equals(oldC.getPlace_birth(), newC.getPlace_birth())) {
            differences.put("Lieu de naissance",
                    diff.apply(oldC.getPlace_birth(), newC.getPlace_birth()));
        }

        if (!Objects.equals(oldC.getGender(), newC.getGender())) {
            differences.put("Sexe",
                    diff.apply(oldC.getGender(), newC.getGender()));
        }

        if (!Objects.equals(oldC.getPhone1(), newC.getPhone1())) {
            differences.put("Téléphone 1",
                    diff.apply(oldC.getPhone1(), newC.getPhone1()));
        }

        if (!Objects.equals(oldC.getPhone2(), newC.getPhone2())) {
            differences.put("Téléphone 2",
                    diff.apply(oldC.getPhone2(), newC.getPhone2()));
        }

        if (!Objects.equals(oldC.getEmail(), newC.getEmail())) {
            differences.put("Email",
                    diff.apply(oldC.getEmail(), newC.getEmail()));
        }

        if (!Objects.equals(oldC.getYear_registry_num(), newC.getYear_registry_num())) {
            differences.put("Année du registre",
                    diff.apply(oldC.getYear_registry_num(), newC.getYear_registry_num()));
        }

        if (!Objects.equals(oldC.getRegistry_num(), newC.getRegistry_num())) {
            differences.put("Numéro du registre",
                    diff.apply(oldC.getRegistry_num(), newC.getRegistry_num()));
        }

        if (!Objects.equals(oldC.getBac_do_count(), newC.getBac_do_count())) {
            differences.put("Nombre de BAC déjà passé",
                    diff.apply(oldC.getBac_do_count(), newC.getBac_do_count()));
        }

        if (!Objects.equals(oldC.getOrigine_bfem(), newC.getOrigine_bfem())) {
            differences.put("Origine BFEM",
                    diff.apply(oldC.getOrigine_bfem(), newC.getOrigine_bfem()));
        }

        if (!Objects.equals(oldC.getYear_bfem(), newC.getYear_bfem())) {
            differences.put("Année BFEM",
                    diff.apply(oldC.getYear_bfem(), newC.getYear_bfem()));
        }

        if (!Objects.equals(oldC.getCentreExamen(), newC.getCentreExamen())) {
            differences.put("Centre d'examen",
                    diff.apply(
                            oldC.getCentreExamen() == null ? null : oldC.getCentreExamen().getName(),
                            newC.getCentreExamen() == null ? null : newC.getCentreExamen().getName()
                    ));
        }

        if (!Objects.equals(oldC.getSubject(), newC.getSubject())) {
            differences.put("Sujet",
                    diff.apply(oldC.getSubject(), newC.getSubject()));
        }

        if (!Objects.equals(oldC.isHandicap(), newC.isHandicap())) {
            differences.put("Handicap",
                    diff.apply(oldC.isHandicap(), newC.isHandicap()));
        }

        if (!Objects.equals(oldC.getType_handicap(), newC.getType_handicap())) {
            differences.put("Type de handicap",
                    diff.apply(oldC.getType_handicap(), newC.getType_handicap()));
        }

        if (!Objects.equals(oldC.getEps(), newC.getEps())) {
            differences.put("EPS",
                    diff.apply(oldC.getEps(), newC.getEps()));
        }

        if (!Objects.equals(oldC.isCdt_is_cgs(), newC.isCdt_is_cgs())) {
            differences.put("Candidat CGS ?",
                    diff.apply(oldC.isCdt_is_cgs(), newC.isCdt_is_cgs()));
        }

        if (!Objects.equals(oldC.getDecision(), newC.getDecision())) {
            differences.put("Décision du dossier",
                    diff.apply(decisionLabel(oldC.getDecision()), decisionLabel(newC.getDecision())));
        }

        if (!Objects.equals(oldC.getMatiere1(), newC.getMatiere1())) {
            differences.put("Matière 1 (LV1 / Spécialité)",
                    diff.apply(
                            oldC.getMatiere1() == null ? null : oldC.getMatiere1().getName(),
                            newC.getMatiere1() == null ? null : newC.getMatiere1().getName()
                    ));
        }

        if (!Objects.equals(oldC.getMatiere2(), newC.getMatiere2())) {
            differences.put("Matière 2 (LV2 / Economie)",
                    diff.apply(
                            oldC.getMatiere2() == null ? null : oldC.getMatiere2().getName(),
                            newC.getMatiere2() == null ? null : newC.getMatiere2().getName()
                    ));
        }

        if (!Objects.equals(oldC.getMatiere3(), newC.getMatiere3())) {
            differences.put("Matière 3 (PC / SVT)",
                    diff.apply(
                            oldC.getMatiere3() == null ? null : oldC.getMatiere3().getName(),
                            newC.getMatiere3() == null ? null : newC.getMatiere3().getName()
                    ));
        }

        if (!Objects.equals(oldC.getEprFacListA(), newC.getEprFacListA())) {
            differences.put("Épreuve facultative Liste A",
                    diff.apply(oldC.getEprFacListA(), newC.getEprFacListA()));
        }

        if (!Objects.equals(oldC.getEprFacListB(), newC.getEprFacListB())) {
            differences.put("Épreuve facultative Liste B",
                    diff.apply(
                            oldC.getEprFacListB() == null ? null : oldC.getEprFacListB().getName(),
                            newC.getEprFacListB() == null ? null : newC.getEprFacListB().getName()
                    ));
        }

        if (!Objects.equals(oldC.getCentreEtatCivil(), newC.getCentreEtatCivil())) {
            differences.put("Centre d'Etat Civil",
                    diff.apply(
                            oldC.getCentreEtatCivil() == null ? null : oldC.getCentreEtatCivil().getName(),
                            newC.getCentreEtatCivil() == null ? null : newC.getCentreEtatCivil().getName()
                    ));
        }

        if (!Objects.equals(oldC.getSerie(), newC.getSerie())) {
            differences.put("Série",
                    diff.apply(
                            oldC.getSerie() == null ? null : oldC.getSerie().getName(),
                            newC.getSerie() == null ? null : newC.getSerie().getName()
                    ));
        }

        if (!Objects.equals(oldC.getNationality(), newC.getNationality())) {
            differences.put("Nationalité",
                    diff.apply(oldC.getNationality(), newC.getNationality()));
        }

        if (!Objects.equals(oldC.getCountryBirth(), newC.getCountryBirth())) {
            differences.put("Pays de naissance",
                    diff.apply(oldC.getCountryBirth(), newC.getCountryBirth()));
        }

        if (!Objects.equals(oldC.getConcoursGeneral(), newC.getConcoursGeneral())) {
            differences.put("Concours Général",
                    diff.apply(oldC.getConcoursGeneral(), newC.getConcoursGeneral()));
        }

        return differences;
    }
}
