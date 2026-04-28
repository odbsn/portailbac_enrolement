package com.officedubac.project.controllers;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.officedubac.project.dto.EtatDeVersementDTO;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.models.EtatDeVersement;
import com.officedubac.project.models.Notification;
import com.officedubac.project.repository.EtablissementRepository;
import com.officedubac.project.repository.EtatDeVersementRepository;
import com.officedubac.project.repository.NotificationRepository;
import com.officedubac.project.services.CandidatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.bson.Document;

import java.io.IOException;
import java.util.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name="File Controller", description = "Endpoints responsables de la gestion des fichiers & documents")
public class FileController
{
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Autowired
    private GridFsOperations operations;

    @Autowired
    private final CandidatService candidatService;

    @Autowired
    private final NotificationRepository notificationRepository;

    @Autowired
    private final EtablissementRepository etablissementRepository;

    @Autowired
    private final EtatDeVersementRepository etatDeVersementRepository;

    @Operation(summary="Service de chargement d'un fichier")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "session", required = false) Long description,
            @RequestParam(value = "establishmentId", required = false) String userId,
            @RequestParam(value = "code", required = false) Integer code
    ) throws IOException {

        // Construire les métadonnées supplémentaires
        Document metadata = new Document();

        if (description != null) {
            metadata.put("session", description);
        }
        if (userId != null) {
            metadata.put("establishmentId", userId);
        }
        metadata.put("uploadDate", new Date());
        metadata.put("code", code);

        // Stocker le fichier avec les métadonnées
        ObjectId id = gridFsTemplate.store(file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                metadata);

        EtatDeVersementDTO evDTO = new EtatDeVersementDTO();
        evDTO.setFile_id(id.toHexString());
        evDTO.setEtablissement(userId);
        evDTO.setSession(description);
        evDTO.setCount_5000(0);
        evDTO.setCount_1000_EF(0);
        candidatService.createEV(evDTO);


        Etablissement etab = etablissementRepository.findById(userId).orElse(null);

        if (etab != null)
        {
            Notification notif = new Notification();
            notif.setMessage("Un nouveau versement a été effectué au Trésor pour l’établissement : " + etab.getName() + ", concernant le baccalauréat " + description);
            notificationRepository.save(notif);
        }


        return ResponseEntity.ok(id.toHexString());
    }

    @Operation(summary="Service de recupération des fichiers chargés par un établissement")
    @GetMapping("/by-etab")
    public ResponseEntity<List<Map<String, Object>>> findFilesBySession(@RequestParam("establishmentId") String establishmentId)
    {
        Query query = new Query();
        // On filtre sur la métadonnée "session"
        query.addCriteria(Criteria.where("metadata.establishmentId").is(establishmentId));

        List<Map<String, Object>> filteredFiles = new ArrayList<>();
        gridFsTemplate.find(query).forEach(gridFSFile -> {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("id", gridFSFile.getObjectId().toHexString());
            fileInfo.put("filename", gridFSFile.getFilename());
            fileInfo.put("length", gridFSFile.getLength());
            fileInfo.put("uploadDate", gridFSFile.getUploadDate());
            fileInfo.put("metadata", gridFSFile.getMetadata());
            filteredFiles.add(fileInfo);
        });

        return ResponseEntity.ok(filteredFiles);
    }

    @Operation(summary="Service de consultation d'un fichier'")
    @GetMapping("/view/{id}")
    public ResponseEntity<?> viewFile(@PathVariable String id) throws IOException {
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        GridFsResource resource = gridFsTemplate.getResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(resource.getInputStream()));
    }


    @Operation(summary="Service de suppression d'un fichier'")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) throws IOException {
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(id)));
        mongoTemplate.remove(
                Query.query(Criteria.where("file_id").is(id)),
                EtatDeVersement.class
        );
        return ResponseEntity.ok().build();
    }
}
