package com.thc.sprbasic2025.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class UtilProperties {
	@Value("${mailbox.host:}")
	private String host;
	@Value("${mailbox.username:}")
	private String username;
	@Value("${mailbox.password:}")
	private String password;
	@Value("${mailbox.smtp-host:}")
	private String smtpHost;
	@Value("${mailbox.smtp-port:587}")
	private String smtpPort;
	@Value("${mailbox.from-address:}")
	private String fromAddress;
	@Value("${mailbox.unsubscribe-base-url:http://localhost:8080}")
	private String unsubscribeBaseUrl;
}
