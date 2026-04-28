package com.officedubac.project.services;

import com.officedubac.project.models.CompteDroitsInscription;
import com.officedubac.project.models.DroitInscription;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.models.Nationality;
import com.officedubac.project.repository.CompteDroitInscriptionRepository;
import com.officedubac.project.repository.DroitsInscriptionRepository;
import com.officedubac.project.repository.EtablissementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaiementService
{
    private final MongoTemplate mongoTemplate;
    @Autowired
    private final DroitsInscriptionRepository droitsInscriptionRepository;
    @Autowired
    private final CompteDroitInscriptionRepository compteDroitInscriptionRepository;
    @Autowired
    private final EtablissementRepository etablissementRepository;

    /**
     * Crée une nouvelle demande de paiement et la persiste en base.
     */
    public Map<String, Object> createPayment(Map<String, Object> request, String etab_code, Long session) {
        String orderNumber = UUID.randomUUID().toString().replace("-", "");


        System.out.println(orderNumber);

        //Etablissement etab = etablissementRepository.findByName(etab_name);
        Map<String, Object> demande = new HashMap<>();
        demande.put("orderNumber", orderNumber);
        demande.put("establishment", etab_code);
        demande.put("dateTransaction", LocalDateTime.now());
        demande.put("session", session);
        demande.put("nbCdtsInscrits", request.get("nbCdtsInscrits"));
        demande.put("montantAVerser", request.get("montantAVerser"));
        demande.put("phoneNumber", request.get("phoneNumber"));
        //A changer une fois le Intouch dispo
//        demande.put("paid", true);
//        demande.put("montantVerser", request.get("montantVerser"));
//        demande.put("paiements", new ArrayList<>());

        mongoTemplate.insert(demande, "droits_inscription");

        Map<String, Object> response = new HashMap<>();
        response.put("orderNumber", orderNumber);
        response.put("amount", request.get("montantAVerser"));
        response.put("phoneNumber", request.get("phoneNumber"));

        //A désactiver une fois le Intouch dispo

//        CompteDroitsInscription cmpt_droit_insc = compteDroitInscriptionRepository.findByEtablissementCodeAndSession(etab_code, session);
//        Etablissement etab = etablissementRepository.findByCode(etab_code);
//        CompteDroitsInscription compteEtab = new CompteDroitsInscription();
//        if (cmpt_droit_insc == null)
//        {
//            compteEtab.setSession(session);
//            compteEtab.setEtablissement(etab);
//            compteEtab.setCount_1000_EF(0);
//            compteEtab.setCount_5000(0);
//            compteEtab.setCount_1000_OB((Integer) demande.get("nbCdtsInscrits"));
//            compteDroitInscriptionRepository.save(compteEtab);
//        }
//        else
//        {
//            cmpt_droit_insc.setCount_1000_OB(cmpt_droit_insc.getCount_1000_OB() + ((Integer) demande.get("nbCdtsInscrits")));
//            compteDroitInscriptionRepository.save(cmpt_droit_insc);
//        }
        return response;
    }

    public List<DroitInscription> getDI(String etabCode, Long session)
    {
        List<DroitInscription> dIns = droitsInscriptionRepository.findByEstablishmentAndSession(etabCode, session);
        return dIns;
    }

    public List<DroitInscription> getAllDroitsInscription(Long session)
    {
        return droitsInscriptionRepository.findBySession(session);
    }



}