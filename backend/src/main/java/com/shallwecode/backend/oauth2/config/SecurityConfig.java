package com.shallwecode.backend.oauth2.config;

import com.shallwecode.backend.oauth2.handler.JwtAccessDeniedHandler;
import com.shallwecode.backend.oauth2.handler.JwtAuthenticationEntryPoint;
import com.shallwecode.backend.oauth2.handler.OAuth2LoginFailureHandler;
import com.shallwecode.backend.oauth2.handler.OAuth2LoginSuccessHandler;
import com.shallwecode.backend.oauth2.jwt.filter.JwtAuthenticationProcessingFilter;
import com.shallwecode.backend.oauth2.jwt.service.JwtService;
import com.shallwecode.backend.oauth2.service.CustomOAuth2UserService;
import com.shallwecode.backend.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable) // httpBasic 사용 X
                .authorizeHttpRequests(auths -> {
                    auths.requestMatchers(
//                            new AntPathRequestMatcher("/user/api/v1/**"),
//                            new AntPathRequestMatcher("/**"),
                            new AntPathRequestMatcher("/"),
                            new AntPathRequestMatcher("/login/**"),
                            new AntPathRequestMatcher("/api/v1/problem/guest", "GET"),
                            new AntPathRequestMatcher("/api/v1/user", "PUT"),
                            new AntPathRequestMatcher("/swagger-ui/index.html"),
                            new AntPathRequestMatcher("/swagger-ui/**"),
                            new AntPathRequestMatcher("/webjars/swagger-ui/**"),
                            new AntPathRequestMatcher("/v3/api-docs/**"),
                            new AntPathRequestMatcher("/v3/api-docs"),
                            new AntPathRequestMatcher("/ws/coding-room/**")
                    ).permitAll();

                    auths.anyRequest().authenticated();
                })
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler) // 동의하고 계속하기를 눌렀을 때 Handler 설정
                        .failureHandler(oAuth2LoginFailureHandler) // 소셜 로그인 실패 시 핸들러 설정
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService) // customUserService 설정
                        )
                );

        http.exceptionHandling(
                exceptionHandling -> {
                    exceptionHandling.accessDeniedHandler(new JwtAccessDeniedHandler());
                    exceptionHandling.authenticationEntryPoint(new JwtAuthenticationEntryPoint());
                }
        );


        // JWT 필터를 추가하여 토큰 관리
        http.addFilterBefore(jwtAuthenticationProcessingFilter(), OAuth2LoginAuthenticationFilter.class);

        /* CORS 설정 */
        http.cors(cors -> cors
                .configurationSource(corsConfigurationSource()));

        return http.build();
    }


    // 로그인 이후에 jwt토큰을 관리하는 필터
    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        JwtAuthenticationProcessingFilter jwtAuthenticationFilter = new JwtAuthenticationProcessingFilter(jwtService);

        return jwtAuthenticationFilter;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // 허용할 도메인
//        config.addAllowedOrigin("http://localhost:80");
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.addAllowedMethod("*"); // 모든 HTTP 메소드 허용
        config.addExposedHeader("accessToken"); // 서버측에서 보내는 헤더에 대한 허용 설정
        config.addExposedHeader("refreshToken");
        config.addExposedHeader("token");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}