package com.example.demo.exam;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class DocumentService {
    
    // Track all temporary files created during processing
    private final List<File> tempFiles = new ArrayList<>();
    
    /**
     * Result class to hold processed file information
     */
    public static class FileExtractionResult {
        private String fileName;
        private String fileType;
        private String content;
        private boolean success;
        private String errorMessage;
        
        // Getters and setters
        public String getFileName() {
            return fileName;
        }
        
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFileType() {
            return fileType;
        }
        
        public void setFileType(String fileType) {
            this.fileType = fileType;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
    
    /**
     * Process multiple files at once
     * @param files List of files to process
     * @return Map of filename to extraction results
     */
    public Map<String, FileExtractionResult> extractContentFromMultipleFiles(List<MultipartFile> files) {
        Map<String, FileExtractionResult> results = new HashMap<>();
        
        try {
            // Process each file
            for (MultipartFile file : files) {
                FileExtractionResult result = new FileExtractionResult();
                
                String fileName = file.getOriginalFilename();
                if (fileName == null) {
                    fileName = "unknown_" + UUID.randomUUID().toString();
                }
                result.setFileName(fileName);
                
                try {
                    // Extract content from the file
                    String fileExtension = getFileExtension(fileName).toLowerCase();
                    result.setFileType(fileExtension);
                    
                    String extractedContent = extractContent(file);
                    result.setContent(extractedContent);
                    result.setSuccess(true);
                } catch (Exception e) {
                    // Log any errors but continue processing other files
                    result.setSuccess(false);
                    result.setErrorMessage("Error processing file: " + e.getMessage());
                }
                
                results.put(fileName, result);
            }
        } finally {
            // Make sure all temp files are cleaned up
            cleanupAllTempFiles();
        }
        
        return results;
    }
    
    /**
     * Extract text content from a single file
     * @param file The uploaded file
     * @return Extracted text content
     * @throws IOException If there's an error processing the file
     */
    public String extractContent(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }
        
        String fileExtension = getFileExtension(fileName).toLowerCase();
        byte[] fileContent = file.getBytes();
        
        try {
            String extractedContent;
            
            switch (fileExtension) {
                case "pdf":
                    String extractedText = extractFromPdf(fileContent);
                    // If we detect it's an image-based PDF, try OCR
                    if (extractedText.contains("[This appears to be an image-only PDF")) {
                        extractedContent = extractFromPdfWithOcr(fileContent);
                    } else {
                        extractedContent = extractedText;
                    }
                    break;
                case "docx":
                    extractedContent = extractFromDocx(fileContent);
                    break;
                case "doc":
                    extractedContent = "Legacy DOC format is not supported. Please convert to DOCX.";
                    break;
                case "txt":
                    extractedContent = new String(fileContent);
                    break;
                case "jpg":
                case "jpeg":
                case "png":
                    extractedContent = extractFromImage(fileContent);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported file format: " + fileExtension);
            }
            
            // Clean up the extracted content
            return cleanExtractedText(extractedContent);
        } catch (Exception e) {
            // Ensure any exception includes the filename for easier debugging
            throw new IOException("Error processing file '" + fileName + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * Clean up all temporary files created during processing
     */
    private void cleanupAllTempFiles() {
        for (File file : tempFiles) {
            try {
                if (file != null && file.exists()) {
                    Files.deleteIfExists(file.toPath());
                }
            } catch (Exception e) {
                // Log the error but continue with other cleanups
                System.err.println("Failed to delete temporary file: " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }
        // Clear the list after cleanup
        tempFiles.clear();
    }
    
    /**
     * Create a temporary file and register it for cleanup
     */
    private File createTrackedTempFile(String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFiles.add(tempFile);
        return tempFile;
    }
    
    /**
     * Clean up extracted text by removing excessive newlines and whitespace
     */
    private String cleanExtractedText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Handle special messages
        if (text.startsWith("[This") && text.endsWith("]")) {
            return text; // Keep informational messages intact
        }
        
        // Replace carriage returns
        String cleaned = text.replace("\r", "");
        
        // Replace consecutive newlines with a single space
        cleaned = Pattern.compile("\n{2,}").matcher(cleaned).replaceAll(" ");
        
        // Replace single newlines with a space
        cleaned = cleaned.replace("\n", " ");
        
        // Replace multiple spaces with a single space
        cleaned = Pattern.compile("\\s+").matcher(cleaned).replaceAll(" ");
        
        // Trim leading/trailing whitespace
        cleaned = cleaned.trim();
        
        return cleaned;
    }
    
    /**
     * Extract text from PDF
     */
    private String extractFromPdf(byte[] content) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(content))) {
            if (document.isEncrypted()) {
                return "[This PDF is encrypted and cannot be processed]";
            }
            
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);
            
            // Check if we got any meaningful text
            if (extractedText == null || extractedText.trim().isEmpty() || extractedText.trim().equals("\r\n")) {
                // Try to count images to determine if it's an image-only PDF
                int imageCount = countImagesInPdf(document);
                if (imageCount > 0) {
                    return "[This appears to be an image-only PDF with " + imageCount + 
                           " images. Consider using OCR processing for this document]";
                } else {
                    return "[No text content could be extracted from this PDF]";
                }
            }
            
            return extractedText;
        }
    }

    private int countImagesInPdf(PDDocument document) {
        try {
            // This is a simple way to estimate image count - not 100% accurate but helpful
            int imageCount = 0;
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                // Each page resource might contain images
                imageCount++;
            }
            return imageCount;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Extract text from DOCX
     */
    private String extractFromDocx(byte[] content) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(content))) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }
    
    /**
     * Extract text from images using OCR
     */
    private String extractFromImage(byte[] content) throws IOException {
        File tempFile = null;
        File convertedFile = null;
        
        try {
            // Save to temporary file for Tesseract to process
            String tempFileName = UUID.randomUUID().toString();
            tempFile = createTrackedTempFile(tempFileName, ".png");
            
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(content);
            }
            
            // Try to validate image format and use a more compatible approach
            BufferedImage bufferedImage = ImageIO.read(tempFile);
            if (bufferedImage == null) {
                throw new IOException("Cannot read image file. Unsupported format or corrupted file.");
            }
            
            // Save the buffered image to a new file with explicit format
            convertedFile = createTrackedTempFile("converted_", ".png");
            ImageIO.write(bufferedImage, "png", convertedFile);
            
            // Use the converted file for OCR
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("D:\\Program Files\\Tess\\tessdata");
            tesseract.setLanguage("eng");
            
            return tesseract.doOCR(convertedFile);
        } catch (Exception e) {
            throw new IOException("Failed to process image: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract text from PDF using OCR if it's an image-based PDF
     */
    private String extractFromPdfWithOcr(byte[] content) throws IOException {
        File tempPdfFile = null;
        
        try {
            // Create temporary PDF file
            tempPdfFile = createTrackedTempFile("temp_pdf_", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(tempPdfFile)) {
                fos.write(content);
            }
            
            // Setup Tesseract
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("D:\\Program Files\\Tess\\tessdata");
            tesseract.setLanguage("eng");
            
            // Process the PDF with OCR
            String result = tesseract.doOCR(tempPdfFile);
            
            return result.isEmpty() ? 
                "[OCR processing completed but no text was found]" : result;
        } catch (TesseractException e) {
            throw new IOException("Error performing OCR on PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
}