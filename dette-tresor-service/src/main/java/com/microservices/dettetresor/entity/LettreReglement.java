package com.microservices.dettetresor.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lettre_reglement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LettreReglement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pret_id", nullable = false)
    @NotNull(message = "Loan reference is required")
    private Pret pret;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_paiement_id") // Removed nullable = false to make it optional
    private OrdrePaiement ordrePaiement;
    
    @Column(name = "numero_lettre", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Letter number is required")
    @Size(max = 50, message = "Letter number cannot exceed 50 characters")
    private String numeroLettre;
    
    @Column(name = "date_transmission", nullable = false)
    @NotNull(message = "Transmission date is required")
    private LocalDate dateTransmission;
    
    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @Column(name = "devise", nullable = false, length = 10)
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @Column(name = "compte_tresor", nullable = false, length = 50)
    @NotBlank(message = "Treasury account is required")
    @Size(max = 50, message = "Treasury account cannot exceed 50 characters")
    private String compteTresor;
    
    @Column(name = "chemin_fichier_pdf", length = 500)
    @Size(max = 500, message = "PDF file path cannot exceed 500 characters")
    private String cheminFichierPdf;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}