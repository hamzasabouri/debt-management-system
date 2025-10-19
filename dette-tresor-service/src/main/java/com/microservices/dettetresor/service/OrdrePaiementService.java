package com.microservices.dettetresor.service;

import com.microservices.dettetresor.dto.OrdrePaiementDTO;

import java.util.List;
import java.util.Map;

public interface OrdrePaiementService {
    OrdrePaiementDTO createOrdrePaiement(OrdrePaiementDTO ordrePaiementDTO);
    OrdrePaiementDTO updateOrdrePaiement(Long id, OrdrePaiementDTO ordrePaiementDTO);
    OrdrePaiementDTO getOrdrePaiementById(Long id);
    OrdrePaiementDTO getOrdrePaiementByNumero(String numeroOrdre);
    List<OrdrePaiementDTO> getAllOrdrePaiements();
    List<OrdrePaiementDTO> getOrdrePaiementsByPretId(Long pretId);
    List<OrdrePaiementDTO> getOrdrePaiementsByLettreReglementId(Long lettreReglementId);
    void deleteOrdrePaiement(Long id);
    List<OrdrePaiementDTO> searchOrdrePaiements(Map<String, String> searchParams);
}
