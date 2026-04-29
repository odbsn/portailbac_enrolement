package com.officedubac.project.module.nouveauBachelier;

import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierAudit;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierRequest;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.text.ParseException;

@Mapper(componentModel = "spring")
public interface NouveauBachelierMapper {
    // transform the entity to PJO class
    NouveauBachelierResponse entiteToResponse(NouveauBachelier nouveauBachelier);

    // transform the entity to PJO class with audit information
    @Mapping(source = "auteurName", target = "auteur")
    @Mapping(source = "modifName", target = "modificateur")
    NouveauBachelierAudit toEntiteAudit(NouveauBachelier nouveauBachelier, Long auteurName, Long modifName);

    // request to entity NouveauBachelier
    NouveauBachelier requestToEntity(NouveauBachelierRequest request);

    // transform the PJO request to an entity
    //@Mapping(source = "user", target = "utiCree")
    NouveauBachelier requestToEntiteAdd(NouveauBachelierRequest nouveauBachelierRequest/*, Utilisateur user*/);   // ici on n'a pa la classe Utilisateur

    // request to existing entity
    //@Mapping(source = "user", target = "utiModifie")
    NouveauBachelier requestToEntiteUp(@MappingTarget NouveauBachelier entity, NouveauBachelierRequest request/*, Utilisateur user*/);

    @Named("formatStringToLong")
    public static Long formatStringToLong(String num) throws NumberFormatException, ParseException {
        return  Long.valueOf(num.trim());
    }

}
