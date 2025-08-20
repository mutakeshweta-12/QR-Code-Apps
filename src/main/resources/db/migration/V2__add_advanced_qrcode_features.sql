-- Migration: Add advanced QR code features to QRCode table
ALTER TABLE QRCode
ADD COLUMN type VARCHAR(32),
ADD COLUMN style VARCHAR(255),
ADD COLUMN scan_count INT DEFAULT 0,
ADD COLUMN last_scanned DATETIME,
ADD COLUMN expires_at DATETIME,
ADD COLUMN is_public BOOLEAN DEFAULT TRUE;

