package com.microservices.dettetresor.service;

import com.microservices.dettetresor.dto.AvisCreditDTO;

import java.util.List;
import java.util.Map;

public interface AvisCreditService {
    AvisCreditDTO createAvisCredit(AvisCreditDTO avisCreditDTO);
    AvisCreditDTO updateAvisCredit(Long id, AvisCreditDTO avisCreditDTO);
    AvisCreditDTO getAvisCreditById(Long id);
    AvisCreditDTO getAvisCreditByNumero(String numeroAvis);
    List<AvisCreditDTO> getAllAvisCredits();
    List<AvisCreditDTO> getAvisCreditsByPretId(Long pretId);
    List<AvisCreditDTO> searchAvisCredits(Map<String, String> searchParams);
    void deleteAvisCredit(Long id);
}