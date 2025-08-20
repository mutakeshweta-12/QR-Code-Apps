package com.qrcode.QR_Code_Apps.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class QRCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;
    private String content;
    private String imagePath;
    private LocalDateTime createdAt;

    private String type; // e.g. TEXT, URL, WIFI, VCARD
    private String style; // e.g. color/style info (JSON or string)
    private Integer scanCount = 0;
    private LocalDateTime lastScanned;
    private LocalDateTime expiresAt;
    private Boolean isPublic = true;

    public QRCode() {}

    public QRCode(User user, String name, String content, String imagePath, LocalDateTime createdAt, String type, String style, Integer scanCount, LocalDateTime lastScanned, LocalDateTime expiresAt, Boolean isPublic) {
        this.user = user;
        this.name = name;
        this.content = content;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
        this.type = type;
        this.style = style;
        this.scanCount = scanCount;
        this.lastScanned = lastScanned;
        this.expiresAt = expiresAt;
        this.isPublic = isPublic;
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public Integer getScanCount() { return scanCount; }
    public void setScanCount(Integer scanCount) { this.scanCount = scanCount; }
    public LocalDateTime getLastScanned() { return lastScanned; }
    public void setLastScanned(LocalDateTime lastScanned) { this.lastScanned = lastScanned; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
}
