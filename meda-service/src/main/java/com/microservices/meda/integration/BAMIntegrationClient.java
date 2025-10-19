package com.microservices.meda.integration;

import com.microservices.meda.dto.AvanceBAMDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@ConditionalOnProperty(name = "meda.integration.bam.enabled", havingValue = "true")
@FeignClient(name = "bam-service", url = "${meda.integration.bam.url}")
public interface BAMIntegrationClient {
    
    @PostMapping("/api/bam/credit-notices/acknowledge")
    void acknowledgeAvanceReception(@RequestBody AvanceBAMDTO avanceBAM);
}