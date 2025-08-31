package com.qrcode.QR_Code_Apps.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class QRCodeControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGenerateQRCode() {
        // Given
        String textToEncode = "HelloWorld";
        String url = "/generate?text=" + textToEncode;

        // When
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        
        // Verify content type is image/png
        assertThat(response.getHeaders().getContentType().toString())
            .isEqualTo("image/png");
    }

    @Test
    void testGenerateQRCode_EmptyText() {
        // Given
        String url = "/generate?text=";

        // When
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
