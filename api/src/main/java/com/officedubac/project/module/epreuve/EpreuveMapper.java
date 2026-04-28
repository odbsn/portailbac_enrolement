package com.officedubac.project.module.epreuve;

import com.officedubac.project.module.epreuve.dto.EpreuveRequest;
import com.officedubac.project.module.epreuve.dto.EpreuveResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {EpreuveMapperUtil.class})
public abstract class EpreuveMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "matiere", source = "matiere", qualifiedByName = "getMatiereById")
    @Mapping(target = "serie", source = "serie", qualifiedByName = "getSerieById")
    @Mapping(target = "jourDebut", source = "jourDebut", qualifiedByName = "getJourById")
    @Mapping(target = "heureDebut", source = "heureDebut", qualifiedByName = "getHeureById")
    public abstract Epreuve toEntity(EpreuveRequest request);

    public abstract EpreuveResponse toResponse(Epreuve epreuve);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "matiere", source = "matiere", qualifiedByName = "getMatiereById")
    @Mapping(target = "serie", source = "serie", qualifiedByName = "getSerieById")
    @Mapping(target = "jourDebut", source = "jourDebut", qualifiedByName = "getJourById")
    @Mapping(target = "heureDebut", source = "heureDebut", qualifiedByName = "getHeureById")
    public abstract void updateEntity(@MappingTarget Epreuve epreuve, EpreuveRequest request);

}