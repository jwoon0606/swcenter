package com.thc.sprbasic2025.util;

import jakarta.mail.Authenticator;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@RequiredArgsConstructor
@Component
public class NewsletterMailSender {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UtilProperties utilProperties;

    public boolean sendNewsletterIssue(String receiverEmail, Integer vol, String title, String detailUrl, String unsubscribeToken) {
        return sendMail(
                receiverEmail,
                buildIssueSubject(vol, title),
                buildIssueBody(vol, title, detailUrl, unsubscribeToken)
        );
    }

    public boolean sendSubscriptionConfirmed(String receiverEmail, String receiverName, String unsubscribeToken) {
        return sendMail(
                receiverEmail,
                buildSubscribeSubject(),
                buildSubscribeBody(receiverName, unsubscribeToken)
        );
    }

    public boolean isConfigured() {
        return getMissingConfigKeys().isEmpty();
    }

    public List<String> getMissingConfigKeys() {
        String username = safeTrim(utilProperties.getUsername());
        String password = safeTrim(utilProperties.getPassword());
        String smtpHost = resolveSmtpHost();
        String fromAddress = safeTrim(utilProperties.getFromAddress());
        if (fromAddress.isEmpty()) {
            fromAddress = username;
        }

        List<String> missing = new ArrayList<>();
        if (username.isEmpty()) {
            missing.add("mailbox.username (MAILBOX_USERNAME)");
        }
        if (password.isEmpty()) {
            missing.add("mailbox.password (MAILBOX_PASSWORD)");
        }
        if (smtpHost.isEmpty()) {
            missing.add("mailbox.smtp-host or mailbox.host (MAILBOX_SMTP_HOST / MAILBOX_HOST)");
        }
        if (fromAddress.isEmpty()) {
            missing.add("mailbox.from-address (MAILBOX_FROM_ADDRESS)");
        }
        return missing;
    }

    private boolean sendMail(String receiverEmail, String subject, String body) {
        String username = safeTrim(utilProperties.getUsername());
        String password = safeTrim(utilProperties.getPassword());
        String smtpHost = resolveSmtpHost();
        String smtpPort = safeTrim(utilProperties.getSmtpPort());
        String fromAddress = safeTrim(utilProperties.getFromAddress());

        if (fromAddress.isEmpty()) {
            fromAddress = username;
        }

        List<String> missingConfig = getMissingConfigKeys();
        if (!missingConfig.isEmpty()) {
            logger.warn("Newsletter mail skipped. mailbox config is incomplete. missing={}", missingConfig);
            return false;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort.isEmpty() ? "587" : smtpPort);
            props.put("mail.smtp.connectiontimeout", "15000");
            props.put("mail.smtp.timeout", "15000");
            props.put("mail.smtp.writetimeout", "15000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail, false));
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");

            Transport.send(message);
            return true;
        } catch (AuthenticationFailedException e) {
            logger.error(
                    "Failed to authenticate SMTP account. host={}, username={}. " +
                    "For Gmail, use an App Password (16-digit) instead of account password.",
                    smtpHost,
                    username
            );
            logger.debug("SMTP authentication error detail", e);
            return false;
        } catch (Exception e) {
            logger.warn("Failed to send newsletter mail. email={}", receiverEmail, e);
            return false;
        }
    }

    private String buildIssueSubject(Integer vol, String title) {
        String titleText = safeTrim(title);
        if (titleText.isEmpty()) {
            titleText = "새 뉴스레터";
        }
        String volText = vol == null ? "" : "Vol." + vol + " ";
        return "[SW중심대학] " + volText + titleText;
    }

    private String buildIssueBody(Integer vol, String title, String detailUrl, String unsubscribeToken) {
        StringBuilder sb = new StringBuilder();
        sb.append("신규 뉴스레터가 발행되어 안내드립니다.\n\n");
        if (vol != null) {
            sb.append("권호: Vol.").append(vol).append("\n");
        }
        if (!safeTrim(title).isEmpty()) {
            sb.append("제목: ").append(title).append("\n");
        }
        sb.append("\n아래 링크에서 확인해주세요.\n");
        sb.append(detailUrl).append("\n\n");
        sb.append(buildUnsubscribeGuide(unsubscribeToken)).append("\n\n");
        sb.append("감사합니다!");
        return sb.toString();
    }

    private String buildSubscribeSubject() {
        return "[SW중심대학] 뉴스레터 구독이 완료되었습니다";
    }

    private String buildSubscribeBody(String receiverName, String unsubscribeToken) {
        String name = safeTrim(receiverName);
        StringBuilder sb = new StringBuilder();
        if (!name.isEmpty()) {
            sb.append(name).append("님, ");
        }
        sb.append("뉴스레터 구독이 완료되었습니다.\n\n");
        sb.append("앞으로 신규 뉴스레터 발행 시 메일로 안내드릴 예정입니다.\n\n");
        sb.append(buildUnsubscribeGuide(unsubscribeToken)).append("\n\n");
        sb.append("감사합니다!");
        return sb.toString();
    }

    private String buildUnsubscribeGuide(String unsubscribeToken) {
        String unsubscribeUrl = buildUnsubscribeUrl(unsubscribeToken);
        return "구독 취소를 원하시면 아래 링크를 클릭해주세요.\n" + unsubscribeUrl;
    }

    private String buildUnsubscribeUrl(String unsubscribeToken) {
        String baseUrl = safeTrim(utilProperties.getUnsubscribeBaseUrl());
        if (baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8080";
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/api/newsletter/unsubscribe?token=" + URLEncoder.encode(safeTrim(unsubscribeToken), StandardCharsets.UTF_8);
    }

    private String resolveSmtpHost() {
        String smtpHost = safeTrim(utilProperties.getSmtpHost());
        if (!smtpHost.isEmpty()) {
            return smtpHost;
        }

        String host = safeTrim(utilProperties.getHost());
        if (host.startsWith("imap.")) {
            return "smtp." + host.substring("imap.".length());
        }
        return host;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
