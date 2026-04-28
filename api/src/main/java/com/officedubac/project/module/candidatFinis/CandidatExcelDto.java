package com.officedubac.project.module.candidatFinis;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
public class CandidatExcelDto {

    // IDENTITÉ
    @ExcelProperty("Prénom(s)")
    @ColumnWidth(20)
    private String prenoms;

    @ExcelProperty("Nom")
    @ColumnWidth(15)
    private String nom;

    @ExcelProperty("Date Nais.")
    @DateTimeFormat("dd/MM/yyyy")
    @ColumnWidth(12)
    private String dateNaissance;

    @ExcelProperty("Lieu de naissance")
    @ColumnWidth(20)
    private String lieuNaissance;

    @ExcelProperty("Nationalité")
    @ColumnWidth(12)
    private String nationalite;

    // EXAMEN
    @ExcelProperty("N° Table")
    @ColumnWidth(10)
    private String numeroTable;

    @ExcelProperty("Jury")
    @ColumnWidth(8)
    private String jury;

    @ExcelProperty("Série")
    @ColumnWidth(8)
    private String serie;

    @ExcelProperty("Se-\nxe")
    @ColumnWidth(5)
    private String sexe;

    @ExcelProperty("Age")
    @ColumnWidth(5)
    private String age;

    @ExcelProperty("EPS")
    @ColumnWidth(10)
    private String eps;

    @ExcelProperty("N°\nDos.")
    @ColumnWidth(8)
    private String numeroDossier;

    @ExcelProperty("Etablissement")
    @ColumnWidth(30)
    private String etablissement;

    @ExcelProperty("Centre d'examen")
    @ColumnWidth(20)
    private String centreExamen;

    // MATIERES OPTIONNELLES
    @ExcelProperty("M.O-1")
    @ColumnWidth(10)
    private String mo1;

    @ExcelProperty("M.O-2")
    @ColumnWidth(10)
    private String mo2;

    @ExcelProperty("M.O-3")
    @ColumnWidth(10)
    private String mo3;

    @ExcelProperty("E-F 1")
    @ColumnWidth(10)
    private String ef1;

    @ExcelProperty("E-F 2")
    @ColumnWidth(10)
    private String ef2;

    @ExcelProperty("Nb. Mat.\nFacult.")
    @ColumnWidth(12)
    private String nbMatFacult;

    @ExcelProperty("I.A")
    @ColumnWidth(5)
    private String ia;

    @ExcelProperty("N.T.I")
    @ColumnWidth(5)
    private String nti;

    @ExcelProperty("Centre d'Ecrit")
    @ColumnWidth(20)
    private String centreEcrit;

    @ExcelProperty("Code\nCES")
    @ColumnWidth(10)
    private String codeCES;

    @ExcelProperty("Crt. Ecrit\nParticulier")
    @ColumnWidth(15)
    private String centreEcritParticulier;

    @ExcelProperty("S. R")
    @ColumnWidth(8)
    private String statutResultat;

    @ExcelProperty("Type\nCandidat")
    @ColumnWidth(12)
    private String typeCandidat;

    @ExcelProperty("C.E.C")
    @ColumnWidth(8)
    private String codeEtatCivil;

    @ExcelProperty("Lib. Etat. Civ")
    @ColumnWidth(20)
    private String libEtatCivil;

    @ExcelProperty("An. Acte.")
    @ColumnWidth(10)
    private String anneeActe;

    @ExcelProperty("Réf.Acte Nais.")
    @ColumnWidth(15)
    private String refActeNaissance;

    @ExcelProperty("Dos. en\nAttentte")
    @ColumnWidth(12)
    private String dossierEnAttente;

    @ExcelProperty("Resultat")
    @ColumnWidth(10)
    private String resultat;

    @ExcelProperty("Raison\nRejet")
    @ColumnWidth(15)
    private String raisonRejet;

    @ExcelProperty("Centre\nAct. EPS")
    @ColumnWidth(15)
    private String centreActEPS;

    @ExcelProperty("Date de \npassage EPS")
    @ColumnWidth(15)
    private String datePassageEPS;

    @ExcelProperty("N.P.\nE.C")
    @ColumnWidth(8)
    private String npEC;

    // ODAE
    @ExcelProperty("Id. d'origine")
    @ColumnWidth(12)
    private String idOrigine;

    @ExcelProperty("Année\nd'ODAE")
    @ColumnWidth(12)
    private String anneeODAE;

    @ExcelProperty("Pays\nd'ODAE")
    @ColumnWidth(12)
    private String paysODAE;

    @ExcelProperty("Identifiant\nà l''ODAE")
    @ColumnWidth(15)
    private String identifiantODAE;

    @ExcelProperty("Série\nà l'ODAE")
    @ColumnWidth(12)
    private String serieODAE;

    // PROVENANCE
    @ExcelProperty("Code Ets.\nProvenance")
    @ColumnWidth(15)
    private String codeEtsProvenance;

    @ExcelProperty("Pas de\nResultat")
    @ColumnWidth(12)
    private String pasDeResultat;

    @ExcelProperty("Classe dans\nl'Ets. de prov.")
    @ColumnWidth(20)
    private String classeEtsProvenance;

    @ExcelProperty("Département\nde provenance")
    @ColumnWidth(20)
    private String departementProvenance;

    @ExcelProperty("Département de\nVille d'examen")
    @ColumnWidth(20)
    private String departementVilleExamen;

    @ExcelProperty("Candidat\ndéplacé")
    @ColumnWidth(12)
    private String candidatDeplace;

    @ExcelProperty("Acad. / Ets.\nProvenance")
    @ColumnWidth(20)
    private String academieProvenance;

    @ExcelProperty("Acad. C.\nEcrit")
    @ColumnWidth(15)
    private String academieEcrit;

    @ExcelProperty("Tél. du \ncandidat")
    @ColumnWidth(12)
    private String telephone;

    @ExcelProperty("Handicap")
    @ColumnWidth(10)
    private String handicap;

    @ExcelProperty("Type de Série\nou de /Filière")
    @ColumnWidth(20)
    private String typeFiliere;

    @ExcelProperty("Session\ndu Jury")
    @ColumnWidth(12)
    private String sessionJury;

    @ExcelProperty("Moy.\nFinale")
    @ColumnWidth(10)
    private String moyenneFinale;

    @ExcelProperty("Mention")
    @ColumnWidth(10)
    private String mention;

    @ExcelProperty("Abs.")
    @ColumnWidth(5)
    private String absence;

    @ExcelProperty("Exclu-\nsion")
    @ColumnWidth(8)
    private String exclusion;

    @ExcelProperty("Titre du sujet de projet\nde soutenance")
    @ColumnWidth(40)
    private String titreProjet;

    @ExcelProperty("Groupe\n/Ets.")
    @ColumnWidth(12)
    private String groupeEts;

    @ExcelProperty("Code Centre \nSoutenance")
    @ColumnWidth(15)
    private String codeCentreSoutenance;

    @ExcelProperty("Lib. centre de Soutenance")
    @ColumnWidth(25)
    private String libCentreSoutenance;

    @ExcelProperty("Ville de soutenance")
    @ColumnWidth(20)
    private String villeSoutenance;

    @ExcelProperty("centre pour\nmat. facult.1")
    @ColumnWidth(20)
    private String centreMatFac1;

    @ExcelProperty("Lib. mat. \nfacult. 1")
    @ColumnWidth(20)
    private String libMatFac1;

    @ExcelProperty("Lib. ville\nmat. facult. 1")
    @ColumnWidth(20)
    private String villeMatFac1;

    @ExcelProperty("centre pour\nmat. facult.2")
    @ColumnWidth(20)
    private String centreMatFac2;

    @ExcelProperty("Lib. mat. \nfacult. 2")
    @ColumnWidth(20)
    private String libMatFac2;

    @ExcelProperty("Ville Mat.\n")
    @ColumnWidth(15)
    private String villeMatFac2;
}
