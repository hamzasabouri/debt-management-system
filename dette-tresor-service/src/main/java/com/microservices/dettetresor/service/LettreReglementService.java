package com.microservices.dettetresor.service;

import com.microservices.dettetresor.dto.LettreReglementDTO;

import java.util.List;
import java.util.Map;

public interface LettreReglementService {
    LettreReglementDTO createLettreReglement(LettreReglementDTO lettreReglementDTO);
    LettreReglementDTO updateLettreReglement(Long id, LettreReglementDTO lettreReglementDTO);
    LettreReglementDTO getLettreReglementById(Long id);
    LettreReglementDTO getLettreReglementByNumero(String numeroLettre);
    List<LettreReglementDTO> getAllLettreReglements();
    List<LettreReglementDTO> getLettreReglementsByPretId(Long pretId);
    List<LettreReglementDTO> searchLettreReglements(Map<String, String> filters);
    void deleteLettreReglement(Long id);
}