package com.microservices.dettetresor.service.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.microservices.dettetresor.dto.LettreReglementDTO;
import com.microservices.dettetresor.dto.OrdrePaiementDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfGenerationService {
    
    @Value("${pdf.output-directory:./pdf-output}")
    private String outputDirectory;
    
    @Value("${pdf.logo-path:static/images/logo.png}")
    private String logoPath;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Generate PDF for lettre de règlement
     */
    public String generateLettreReglementPdf(LettreReglementDTO lettreReglement, OrdrePaiementDTO ordrePaiement) {
        try {
            // Ensure output directory exists
            Path outputPath = Paths.get(outputDirectory);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
            
            // Generate filename
            String filename = String.format("lettre_reglement_%s_%s.pdf", 
                lettreReglement.getNumeroLettre(), 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            String fullPath = Paths.get(outputDirectory, filename).toString();
            
            // Create PDF document
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            
            document.open();
            
            // Add content
            addHeader(document);
            addLettreReglementContent(document, lettreReglement, ordrePaiement);
            addFooter(document);
            
            document.close();
            
            log.info("PDF generated successfully: {}", fullPath);
            return fullPath;
            
        } catch (Exception e) {
            log.error("Error generating PDF for lettre de règlement: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }
    
    /**
     * Generate PDF as byte array for download
     */
    public byte[] generateLettreReglementPdfBytes(LettreReglementDTO lettreReglement, OrdrePaiementDTO ordrePaiement) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Add content
            addHeader(document);
            addLettreReglementContent(document, lettreReglement, ordrePaiement);
            addFooter(document);
            
            document.close();
            
            log.info("PDF bytes generated successfully for lettre: {}", lettreReglement.getNumeroLettre());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating PDF bytes for lettre de règlement: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF bytes: " + e.getMessage());
        }
    }
    
    private void addHeader(Document document) throws DocumentException {
        try {
            // Try to add logo
            ClassPathResource logoResource = new ClassPathResource(logoPath);
            if (logoResource.exists()) {
                Image logo = Image.getInstance(logoResource.getURL());
                logo.scaleToFit(100, 100);
                logo.setAlignment(Element.ALIGN_LEFT);
                
                PdfPTable headerTable = new PdfPTable(2);
                headerTable.setWidthPercentage(100);
                float[] columnWidths = {1f, 5f};
                headerTable.setWidths(columnWidths);
                
                PdfPCell logoCell = new PdfPCell(logo);
                logoCell.setBorder(Rectangle.NO_BORDER);
                logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                headerTable.addCell(logoCell);
                
                // Add text content in the second cell
                PdfPCell textCell = new PdfPCell();
                textCell.setBorder(Rectangle.NO_BORDER);
                
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                Paragraph title = new Paragraph("ROYAUME DU MAROC", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                textCell.addElement(title);
                
                Paragraph subtitle = new Paragraph("MINISTÈRE DE L'ÉCONOMIE ET DES FINANCES", titleFont);
                subtitle.setAlignment(Element.ALIGN_CENTER);
                textCell.addElement(subtitle);
                
                Paragraph department = new Paragraph("TRÉSORERIE GÉNÉRALE DU ROYAUME", titleFont);
                department.setAlignment(Element.ALIGN_CENTER);
                textCell.addElement(department);
                
                headerTable.addCell(textCell);
                document.add(headerTable);
            } else {
                // Fallback to text-only header if logo doesn't exist
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                Paragraph title = new Paragraph("ROYAUME DU MAROC", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                
                Paragraph subtitle = new Paragraph("MINISTÈRE DE L'ÉCONOMIE ET DES FINANCES", titleFont);
                subtitle.setAlignment(Element.ALIGN_CENTER);
                document.add(subtitle);
                
                Paragraph department = new Paragraph("TRÉSORERIE GÉNÉRALE DU ROYAUME", titleFont);
                department.setAlignment(Element.ALIGN_CENTER);
                document.add(department);
            }
        } catch (Exception e) {
            log.warn("Could not load logo, using text-only header: {}", e.getMessage());
            // Fallback to text-only header if logo loading fails
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("ROYAUME DU MAROC", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph subtitle = new Paragraph("MINISTÈRE DE L'ÉCONOMIE ET DES FINANCES", titleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            
            Paragraph department = new Paragraph("TRÉSORERIE GÉNÉRALE DU ROYAUME", titleFont);
            department.setAlignment(Element.ALIGN_CENTER);
            document.add(department);
        }
        
        document.add(new Paragraph("\n"));
        
        // Letter title
        Font letterTitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Paragraph letterTitle = new Paragraph("LETTRE DE RÈGLEMENT", letterTitleFont);
        letterTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(letterTitle);
        
        document.add(new Paragraph("\n"));
    }
    
    private void addLettreReglementContent(Document document, LettreReglementDTO lettreReglement, 
                                         OrdrePaiementDTO ordrePaiement) throws DocumentException {
        
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        
        // Letter details table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Letter number
        addTableRow(table, "Numéro de la lettre :", lettreReglement.getNumeroLettre(), boldFont, normalFont);
        
        // Date
        addTableRow(table, "Date de transmission :", 
            lettreReglement.getDateTransmission().format(DATE_FORMATTER), boldFont, normalFont);
        
        // Payment order number
        addTableRow(table, "Numéro d'ordre de paiement :", ordrePaiement.getNumeroOrdre(), boldFont, normalFont);
        
        // Amount
        addTableRow(table, "Montant :", 
            String.format("%.2f %s", lettreReglement.getMontant(), lettreReglement.getDevise()), 
            boldFont, normalFont);
        
        // Currency
        addTableRow(table, "Devise :", lettreReglement.getDevise(), boldFont, normalFont);
        
        // Treasury account
        addTableRow(table, "Compte du Trésor :", lettreReglement.getCompteTresor(), boldFont, normalFont);
        
        // Due date
        addTableRow(table, "Date d'échéance :", 
            ordrePaiement.getEcheance().format(DATE_FORMATTER), boldFont, normalFont);
        
        document.add(table);
        
        // Instructions
        document.add(new Paragraph("\n"));
        Paragraph instructions = new Paragraph(
            "Nous vous prions de bien vouloir procéder au débit du compte du Trésor susmentionné " +
            "du montant indiqué ci-dessus, en vue du règlement de l'échéance de la dette extérieure " +
            "conformément à l'ordre de paiement de référence.", normalFont);
        instructions.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(instructions);
        
        document.add(new Paragraph("\n"));
        Paragraph gratitude = new Paragraph(
            "Nous vous remercions par avance de votre diligence et vous prions d'agréer, " +
            "Monsieur le Gouverneur, l'expression de notre haute considération.", normalFont);
        gratitude.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(gratitude);
    }
    
    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5f);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5f);
        table.addCell(valueCell);
    }
    
    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph("\n\n"));
        
        Font signatureFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Paragraph signature = new Paragraph("Le Trésorier Général du Royaume", signatureFont);
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);
        
        document.add(new Paragraph("\n\n"));
        
        // Footer note
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
        Paragraph footer = new Paragraph(
            "Cette lettre est générée automatiquement par le système de gestion de la dette du Trésor.", 
            footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
}