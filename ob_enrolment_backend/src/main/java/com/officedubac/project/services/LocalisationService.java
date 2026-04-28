package com.officedubac.project.services;

import com.officedubac.project.dto.CentreEtatCivilDTO;
import com.officedubac.project.dto.MatiereDTO;
import com.officedubac.project.models.CentreEtatCivil;
import com.officedubac.project.models.Matiere;
import com.officedubac.project.repository.CentreEtatCivilRepository;
import com.officedubac.project.repository.DepartementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalisationService
{

    @Autowired
    private final CentreEtatCivilRepository centreEtatCivilRepository;

    @Autowired
    private final DepartementRepository departementRepository;

    public CentreEtatCivil createCentreEtatCivil(CentreEtatCivilDTO centreEtatCivilDTO)
    {
        CentreEtatCivil cec = CentreEtatCivil.builder()
                .name(centreEtatCivilDTO.getName())
                .code(centreEtatCivilDTO.getCode())
                .departement(departementRepository.findByName(centreEtatCivilDTO.getDepartement().getName()))
                .build();
        return centreEtatCivilRepository.save(cec);
    }

    public List<CentreEtatCivilDTO> getCentreEtatCivil()
    {
        List<CentreEtatCivil> cecs = centreEtatCivilRepository.findAll();
        return cecs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CentreEtatCivilDTO convertToDTO(CentreEtatCivil cec) {
        CentreEtatCivilDTO dto = new CentreEtatCivilDTO();

        dto.setCode(cec.getCode());
        dto.setName(cec.getName());
        dto.setDepartement(cec.getDepartement());
        return dto;
    }

}
