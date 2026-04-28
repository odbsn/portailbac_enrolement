package com.officedubac.project.module.candidatFinis;

import com.officedubac.project.models.Etablissement;
import com.officedubac.project.models.Ville;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "candidat_finis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatFinis {

    @Id
    private String id;

    // IDENTITÉ
    private String prenoms;
    private String nom;
    private String dateNaissance;
    private String lieuNaissance;
    private String nationalite;

    // EXAMEN
    private String numeroTable;
    private String jury;
    private String serie;
    private String sexe;
    private Integer age;

    private String eps;
    private String numeroDossier;
    private Etablissement etablissement;
    private Ville centreExamen;
    private Etablissement centreActEPS;

    // MATIERES OPTIONNELLES
    private String mo1;
    private String mo2;
    private String mo3;

    private String ef1;
    private String ef2;

    private Integer nbMatFacult;

    private Integer ia;
    private Integer nti;

    private Etablissement centreEcrit;
    private String codeCES;
    private String centreEcritParticulier;

    private String statutResultat;
    private String typeCandidat;

    private String codeEtatCivil;
    private String libEtatCivil;
    private String anneeActe;
    private String refActeNaissance;

    private String dossierEnAttente;

    private String resultat;
    private String raisonRejet;
    private String datePassageEPS;

    private String npEC;

    // ODAE
    private String idOrigine;
    private String anneeODAE;
    private String paysODAE;
    private String identifiantODAE;
    private String serieODAE;

    // PROVENANCE
    private String codeEtsProvenance;
    private String pasDeResultat;
    private String classeEtsProvenance;
    private String departementProvenance;
    private String departementVilleExamen;

    private String candidatDeplace;
    private String academieProvenance;
    private String academieEcrit;

    private String telephone;

    private String handicap;
    private String typeFiliere;

    private String sessionJury;

    private Double moyenneFinale;
    private String mention;

    private String absence;
    private String exclusion;

    private String titreProjet;

    private String groupeEts;

    private String codeCentreSoutenance;
    private String libCentreSoutenance;
    private String villeSoutenance;

    private String centreMatFac1;
    private String libMatFac1;
    private String villeMatFac1;

    private String centreMatFac2;
    private String libMatFac2;
    private String villeMatFac2;
}
