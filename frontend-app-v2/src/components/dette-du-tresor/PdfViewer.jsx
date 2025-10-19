import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Typography
} from '@mui/material';
import {
  Close as CloseIcon,
  Download as DownloadIcon,
  Print as PrintIcon
} from '@mui/icons-material';
import { generateSettlementLetterPdf } from '../../services/dette-tresor/settlementLettersService';

/**
 * PDF Viewer component for Settlement Letters
 * Displays a PDF in a dialog and allows printing or downloading
 */
const PdfViewer = ({ open, onClose, letterId, letterNumber }) => {
  const [loading, setLoading] = useState(true);
  const [pdfUrl, setPdfUrl] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (open && letterId) {
      loadPdf();
    }
    return () => {
      // Cleanup URL object when component unmounts
      if (pdfUrl) {
        URL.revokeObjectURL(pdfUrl);
      }
    };
  }, [open, letterId]);

  const loadPdf = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Generate PDF and get blob for viewing
      const pdfBlob = await generateSettlementLetterPdf(letterId, true);
      
      // Create URL from blob
      const url = URL.createObjectURL(new Blob([pdfBlob], { type: 'application/pdf' }));
      setPdfUrl(url);
    } catch (error) {
      console.error('Error loading PDF:', error);
      // Provide more specific error messages based on the error type
      let errorMessage = 'Impossible de charger le PDF. Veuillez réessayer plus tard.';
      if (error.message.includes('not found') || error.message.includes('404')) {
        errorMessage = 'Lettre de règlement non trouvée ou aucun ordre de paiement associé. Veuillez associer un ordre de paiement à cette lettre avant de générer le PDF.';
      } else if (error.message.includes('Internal server error') || error.message.includes('500')) {
        errorMessage = 'Erreur serveur lors de la génération du PDF. Veuillez réessayer ou contacter l\'administrateur.';
      } else if (error.message.includes('API request failed') || error.message.includes('Network error')) {
        errorMessage = 'Erreur de communication avec le serveur. Veuillez vérifier votre connexion et réessayer.';
      }
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = () => {
    if (pdfUrl) {
      const printWindow = window.open(pdfUrl, '_blank');
      printWindow.addEventListener('load', () => {
        printWindow.print();
      });
    }
  };

  const handleDownload = () => {
    if (pdfUrl) {
      const link = document.createElement('a');
      link.href = pdfUrl;
      link.setAttribute('download', `lettre-reglement-${letterNumber || letterId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="lg"
      fullWidth
      PaperProps={{
        sx: {
          height: '90vh',
          display: 'flex',
          flexDirection: 'column'
        }
      }}
    >
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">
          Lettre de Règlement {letterNumber ? `N° ${letterNumber}` : ''}
        </Typography>
        <Box>
          <IconButton onClick={handlePrint} disabled={loading || !!error} title="Imprimer">
            <PrintIcon />
          </IconButton>
          <IconButton onClick={handleDownload} disabled={loading || !!error} title="Télécharger">
            <DownloadIcon />
          </IconButton>
          <IconButton onClick={onClose} title="Fermer">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>
      
      <DialogContent sx={{ flexGrow: 1, padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
            <Paper elevation={0} sx={{ p: 3, textAlign: 'center' }}>
              <Typography color="error" gutterBottom>{error}</Typography>
              <Button variant="outlined" onClick={loadPdf}>Réessayer</Button>
            </Paper>
          </Box>
        ) : (
          <iframe
            src={`${pdfUrl}#toolbar=0`}
            title={`Lettre de Règlement ${letterNumber || letterId}`}
            width="100%"
            height="100%"
            style={{ border: 'none' }}
          />
        )}
      </DialogContent>
    </Dialog>
  );
};

export default PdfViewer;