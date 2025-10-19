package com.microservices.dettetresor.service;

import com.microservices.dettetresor.dto.AvisDebitDTO;

import java.util.List;
import java.util.Map;

public interface AvisDebitService {
    AvisDebitDTO createAvisDebit(AvisDebitDTO avisDebitDTO);
    AvisDebitDTO updateAvisDebit(Long id, AvisDebitDTO avisDebitDTO);
    AvisDebitDTO getAvisDebitById(Long id);
    AvisDebitDTO getAvisDebitByNumero(String numeroAvis);
    List<AvisDebitDTO> getAllAvisDebits();
    List<AvisDebitDTO> getAvisDebitsByPretId(Long pretId);
    List<AvisDebitDTO> searchAvisDebits(Map<String, String> filters);
    void deleteAvisDebit(Long id);
}
