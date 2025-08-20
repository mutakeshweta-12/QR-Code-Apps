package com.qrcode.QR_Code_Apps.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.qrcode.QR_Code_Apps.entity.QRCode;
import com.qrcode.QR_Code_Apps.entity.User;
import com.qrcode.QR_Code_Apps.repository.QRCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class QRCodeService {

    @Autowired
    private QRCodeRepository qrCodeRepository;

    public void generateQRCode(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            log.error("mobile cannot be null or empty");
            return;
        }

        int width = 500;
        int height = 500;
        String filePath = mobile + ".png";

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(mobile, BarcodeFormat.QR_CODE, width, height);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            log.info("QR Code generated successfully: " + filePath);
        } catch (WriterException | IOException e) {
            log.error("Error generating QR Code", e);
        }
    }

    /**
     * Generate QR code with custom content and filename
     */
    public void generateQRCodeWithContent(String filename, String content) {
        if (filename == null || filename.isEmpty() || content == null || content.isEmpty()) {
            log.error("Filename and content cannot be null or empty");
            return;
        }

        int width = 500;
        int height = 500;

        String filePath = filename + ".png";

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            log.info("QR Code generated successfully for user: " + filePath);
        } catch (WriterException | IOException e) {
            log.error("Error generating QR Code for user", e);
        }
    }



    /**
     * Get the file path of a user's QR code
     */
    public String getUserQRCodePath(String mobile) {
        return mobile + ".png";
    }

    /**
     * Check if QR code exists for a user
     */
    public boolean qrCodeExists(String mobile) {
        File file = new File(getUserQRCodePath(mobile));
        return file.exists();
    }

    /**
     * Decode QR code from uploaded file and extract user information
     */
    public String decodeQRCodeFromFile(MultipartFile file) {
        try {
            // Convert MultipartFile to BufferedImage
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            if (bufferedImage == null) {
                log.error("Could not read image from uploaded file");
                return null;
            }

            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new QRCodeReader().decode(bitmap);

            log.info("Successfully decoded QR code: " + result.getText());
            return result.getText();

        } catch (IOException | NotFoundException | ChecksumException | FormatException e) {
            log.error("Error decoding QR code from uploaded file", e);
            return null;
        }
    }

    /**
     * Extract mobile number from decoded QR code content
     */
    public String extractMobileFromQRContent(String qrContent) {
        if (qrContent == null || qrContent.isEmpty()) {
            return null;
        }

        // Parse the QR content to extract mobile number
        // Expected format: "Name: John Doe\nEmail: john@example.com\nMobile: 1234567890\nUser ID: 1"
        String[] lines = qrContent.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("Mobile:")) {
                return line.substring(line.indexOf(":") + 1).trim();
            }
        }
        return null;
    }

    /**
     * Extract user ID from decoded QR code content
     */
    public Integer extractUserIdFromQRContent(String qrContent) {
        if (qrContent == null || qrContent.isEmpty()) {
            return null;
        }

        String[] lines = qrContent.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("User ID:")) {
                try {
                    return Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
                } catch (NumberFormatException e) {
                    log.error("Invalid User ID format in QR code", e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Read and decode QR code from file path (public method for UserService)
     */
    public String getQR(String filePath) throws IOException, NotFoundException, ChecksumException, FormatException {
        BufferedImage bufferedImage = ImageIO.read(new java.io.File(filePath));
        LuminanceSource source = new com.google.zxing.client.j2se.BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));
        Result result = new com.google.zxing.qrcode.QRCodeReader().decode(bitmap);
        return result.getText();
    }

    /**
     * Generate QR code as Base64 string for embedding in HTML
     */
    public String generateQRCodeBase64(String content) {
        if (content == null || content.isEmpty()) {
            log.error("QR content cannot be null or empty");
            return null;
        }
        int width = 500;
        int height = 500;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            return "data:image/png;base64," + base64;
        } catch (WriterException | IOException e) {
            log.error("Error generating QR Code as base64", e);
            return null;
        }
    }

    public List<QRCode> getAllQRCodesForUser(User user) {
        return qrCodeRepository.findAllByUser(user);
    }

    public QRCode createQRCodeForUser(User user, String name, String content, String type, String style, LocalDateTime expiresAt, Boolean isPublic) {
        try {
            // Generate unique filename
            String filename = user.getUserId() + "_" + System.currentTimeMillis();
            String imagePath = "qrcodes/" + filename + ".png";
            int width = 500;
            int height = 500;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            File dir = new File("qrcodes");
            if (!dir.exists()) dir.mkdirs();
            Path path = FileSystems.getDefault().getPath(imagePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            QRCode qrCode = new QRCode();
            qrCode.setUser(user);
            qrCode.setName(name);
            qrCode.setContent(content);
            qrCode.setImagePath("/" + imagePath);
            qrCode.setCreatedAt(LocalDateTime.now());
            qrCode.setType(type);
            qrCode.setStyle(style);
            qrCode.setExpiresAt(expiresAt);
            qrCode.setIsPublic(isPublic != null ? isPublic : true);
            qrCode.setScanCount(0);
            qrCode.setLastScanned(null);
            return qrCodeRepository.save(qrCode);
        } catch (Exception e) {
            log.error("Error creating QR code for user", e);
            return null;
        }
    }

    public void incrementScanAnalytics(QRCode qrCode) {
        qrCode.setScanCount(qrCode.getScanCount() == null ? 1 : qrCode.getScanCount() + 1);
        qrCode.setLastScanned(LocalDateTime.now());
        qrCodeRepository.save(qrCode);
    }

    public boolean deleteQRCodeById(Integer id, User user) {
        Optional<QRCode> qrOpt = qrCodeRepository.findById(id);
        if (qrOpt.isPresent() && qrOpt.get().getUser().getUserId().equals(user.getUserId())) {
            QRCode qr = qrOpt.get();
            // Delete image file
            if (qr.getImagePath() != null) {
                File file = new File("." + qr.getImagePath());
                if (file.exists()) file.delete();
            }
            qrCodeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean renameQRCode(Integer id, String newName, User user) {
        Optional<QRCode> qrOpt = qrCodeRepository.findById(id);
        if (qrOpt.isPresent() && qrOpt.get().getUser().getUserId().equals(user.getUserId())) {
            QRCode qr = qrOpt.get();
            qr.setName(newName);
            qrCodeRepository.save(qr);
            return true;
        }
        return false;
    }

    public Optional<QRCode> getQRCodeById(Integer id, User user) {
        Optional<QRCode> qrOpt = qrCodeRepository.findById(id);
        if (qrOpt.isPresent() && qrOpt.get().getUser().getUserId().equals(user.getUserId())) {
            return qrOpt;
        }
        return Optional.empty();
    }
}
