package com.officedubac.project.module.candidatFinis;

import com.officedubac.project.module.candidatFinis.dto.CandidatFinisRequest;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisResponse;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CandidatFinisMapperUtil.class})
@Component
public interface CandidatFinisMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "etablissement", source = "etablissement", qualifiedByName = "getEtablissementById")
//    @Mapping(target = "centreExamen", source = "centreExamen", qualifiedByName = "toVille")
    @Mapping(target = "centreActEPS", source = "centreActEPS", qualifiedByName = "getEtablissementById")
    @Mapping(target = "centreEcrit", source = "centreEcrit", qualifiedByName = "getEtablissementById")
    @Mapping(target = "age", ignore = true)
    @Mapping(target = "statutResultat", ignore = true)
    CandidatFinis toEntity(CandidatFinisRequest request);

    CandidatFinisResponse toResponse(CandidatFinis entity);

    List<CandidatFinisResponse> toResponseList(List<CandidatFinis> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "etablissement", source = "etablissement", qualifiedByName = "getEtablissementById")
//    @Mapping(target = "centreExamen", source = "centreExamen", qualifiedByName = "toVille")
    @Mapping(target = "centreActEPS", source = "centreActEPS", qualifiedByName = "getEtablissementById")
    @Mapping(target = "centreEcrit", source = "centreEcrit", qualifiedByName = "getEtablissementById")
    @Mapping(target = "age", ignore = true)
    @Mapping(target = "statutResultat", ignore = true)
   void updateEntity(@MappingTarget CandidatFinis entity, CandidatFinisRequest request);

}
