package com.microservices.meda.integration;

import com.microservices.meda.dto.PieceJustificativeComptabiliteDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@ConditionalOnProperty(name = "meda.integration.comptabilite.enabled", havingValue = "true")
@FeignClient(name = "comptabilite-service", url = "${meda.integration.comptabilite.url}")
public interface ComptabiliteIntegrationClient {
    
    @PostMapping("/api/comptabilite/documents/acknowledge")
    void acknowledgePieceJustificativeReception(@RequestBody PieceJustificativeComptabiliteDTO piece);
}