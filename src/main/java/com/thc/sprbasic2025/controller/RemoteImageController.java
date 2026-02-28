package com.thc.sprbasic2025.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/image")
public class RemoteImageController {

    private static final Set<String> ALLOWED_HOSTS = Set.of("walab.info", "www.walab.info");

    @GetMapping("/remote")
    public ResponseEntity<byte[]> remoteImage(@RequestParam("u") String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            URI uri = URI.create(imageUrl);
            String host = uri.getHost();
            if (host == null || !ALLOWED_HOSTS.contains(host.toLowerCase())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            URL url = uri.toURL();
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Referer", "https://walab.info/");

            byte[] body;
            try (InputStream in = connection.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                body = out.toByteArray();
            }

            String contentType = connection.getContentType();
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (StringUtils.hasText(contentType)) {
                try {
                    mediaType = MediaType.parseMediaType(contentType);
                } catch (Exception ignored) {
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setCacheControl(CacheControl.maxAge(6, TimeUnit.HOURS).cachePublic().getHeaderValue());
            return new ResponseEntity<>(body, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
                    .body("image fetch failed".getBytes(StandardCharsets.UTF_8));
        }
    }
}
