package com.qrcode.QR_Code_Apps.repository;

import com.qrcode.QR_Code_Apps.entity.QRCode;
import com.qrcode.QR_Code_Apps.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QRCodeRepository extends JpaRepository<QRCode, Integer> {
    List<QRCode> findAllByUser(User user);
}
