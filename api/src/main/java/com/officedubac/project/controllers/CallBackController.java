package com.officedubac.project.controllers;

import com.mongodb.client.result.UpdateResult;
import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.models.CompteDroitsInscription;
import com.officedubac.project.models.DroitInscription;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.models.PaiementFAEB3;
import com.officedubac.project.repository.CompteDroitInscriptionRepository;
import com.officedubac.project.repository.EtablissementRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name="CallBack Controller", description = "Endpoints responsables du callback des transactions")
public class CallBackController
{

    private final MongoTemplate mongoTemplate;
    private final CompteDroitInscriptionRepository compteDroitInscriptionRepository;
    @Autowired
    private final EtablissementRepository etablissementRepository;
    @Operation(summary="CallBack destiné à InTouch")
    @PostMapping("/callback")
    public String handleCallback(
            @RequestParam String payment_mode,
            @RequestParam String paid_sum,
            @RequestParam String paid_amount,
            @RequestParam String payment_token,
            @RequestParam String payment_status,
            @RequestParam String command_number,
            @RequestParam String payment_validation_date)
    {
        // Validation et logging
        log.info("Callback reçu : mode={}, payé={}, dû={}, token={}, statut={}, commande={}, date={}",
                payment_mode, paid_sum, paid_amount, payment_token, payment_status, command_number, payment_validation_date);

        double montantPaye;
        long timestamp;
        try
        {
            montantPaye = Double.parseDouble(paid_sum);
            timestamp = Long.parseLong(payment_validation_date);
        }
        catch (NumberFormatException e)
        {
            log.error("Montant ou date invalide", e);
            throw new BusinessResourceException("Montant ou date de validation du paiement invalide.");
        }

        LocalDateTime datePaiement = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

        // Création de l'objet Paiement
        PaiementFAEB3 paiement = new PaiementFAEB3();
        paiement.setOrderNumber(command_number);
        paiement.setPaidSum(montantPaye);
        paiement.setPaymentMode(payment_mode);
        paiement.setPaymentValidationDate(datePaiement);
        paiement.setPaymentStatus(payment_status.equals("200") ? "succes" : "échec");

        // Préparation de la requête et update
        Query query = new Query(Criteria.where("orderNumber").is(command_number));
        Update update = new Update().set("paiement", paiement);

        if (payment_status.equals("200"))
        {
            update.set("montantVerser", montantPaye).set("paid", true);
            UpdateResult result = mongoTemplate.updateFirst(query, update, DroitInscription.class);
            if (result.getMatchedCount() == 0)
            {
                log.error("Demande non trouvée : {}", command_number);
                throw new BusinessResourceException("Demande non trouvée avec le code spécifié.");
            }

            // Récupération de la demande mise à jour
            DroitInscription droit = mongoTemplate.findOne(query, DroitInscription.class);
            if (droit == null)
            {
                throw new BusinessResourceException("Demande introuvable après mise à jour.");
            }

            // Gestion du compte établissement
            String etabCode = droit.getEstablishment();
            Long session = droit.getSession();
            int nbInscrits = droit.getNbCdtsInscrits();

            CompteDroitsInscription compte = compteDroitInscriptionRepository.findByEtablissementCodeAndSession(etabCode, session);
            Etablissement etab = etablissementRepository.findByCode(etabCode);

            if (compte == null)
            {
                compte = new CompteDroitsInscription();
                compte.setSession(session);
                compte.setEtablissement(etab);
                compte.setCount_1000_EF(0);
                compte.setCount_5000(0);
                compte.setCount_1000_OB(nbInscrits);
            }
            else
            {
                compte.setCount_1000_OB(compte.getCount_1000_OB() + nbInscrits);
            }
            compteDroitInscriptionRepository.save(compte);
            log.info("Callback traité avec succès pour commande {}", command_number);
        }
        else
        {
            update.set("paid", false);
        }

        return payment_status;
    }
}