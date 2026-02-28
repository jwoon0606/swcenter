package com.thc.sprbasic2025.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thc.sprbasic2025.repository.UserRepository;
import com.thc.sprbasic2025.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
	
	private final UserRepository userRepository;
	private final CorsFilterConfiguration corsFilterConfiguration;
	private final ObjectMapper objectMapper;
	private final AuthService authService; // 토큰 팩토리라고 생각해주세요!
	private final ExternalProperties externalProperties;

	public SecurityConfig(UserRepository userRepository, CorsFilterConfiguration corsFilterConfiguration, ObjectMapper objectMapper, AuthService authService
			, ExternalProperties externalProperties) {
		this.userRepository = userRepository;
		this.corsFilterConfiguration = corsFilterConfiguration;
		this.objectMapper = objectMapper;
		this.authService = authService;
		this.externalProperties = externalProperties;
	}

	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
    /**
	 *  Spring Security 권한 설정.
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.headers(headers -> headers
				.frameOptions(frameOptions -> frameOptions.sameOrigin())
				.crossOriginOpenerPolicy(crossOriginOpenerPolicy -> crossOriginOpenerPolicy
						.policy(CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN_ALLOW_POPUPS))
		);
		http
				.csrf(AbstractHttpConfigurer::disable)
					.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				//세션을 안쓰는 경우!!	STATELESS!!
				.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.formLogin(AbstractHttpConfigurer::disable)
					.httpBasic(AbstractHttpConfigurer::disable)
					.addFilter(corsFilterConfiguration.corsFilter())
				.apply(new CustomDsl());
		return http.build();
	}
					
	public class CustomDsl extends AbstractHttpConfigurer<CustomDsl, HttpSecurity> {
		
	    /**
		 *  Jwt Token Authentication을 위한 filter 설정.
		 *  
		 *  JwtAuthorizationFilter: 인가를 위한 필터
		 *  FilterExceptionHandlerFilter: TokenExpiredException 핸들링을 위한 필터 
		 */
		@Override
		public void configure(HttpSecurity http) throws Exception {
			AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
			
			http.addFilter(corsFilterConfiguration.corsFilter())
				.addFilter(new JwtAuthorizationFilter(authenticationManager, userRepository, authService, externalProperties))
				.addFilterBefore(new FilterExceptionHandlerFilter(), BasicAuthenticationFilter.class);
		}
		
	}

}
