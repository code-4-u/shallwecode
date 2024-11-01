package com.shallwecode.backend.security.util;

import com.shallwecode.backend.user.application.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    private final Key key;
    private final UserService userService;

    public JwtUtil(@Value("${token.secret}") String secretKey, UserService userService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userService = userService;
    }

    // Token 검증
    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token {}", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token {}", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token {}", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT Token claims empty {}", e);
        }

        return false;
    }

    // 넘어온 AccessToken으로 인증 객체 추출
    public Authentication getAuthentication(String token) {

        /* 토큰을 들고 왔던 들고 오지 않았던(로그인 시) 동일하게 security가 관리 할 UserDetails 타입을 정의 */
        UserDetails userDetails = userService.loadUserByUsername(this.getUserId(token));

        Claims claims = parseClaims(token);
        log.info("넘어온 AccessToken claims 확인: {}", claims);

        Collection<? extends GrantedAuthority> authorities = null;
        if(claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        } else {
             authorities =
                     Arrays.stream(claims.get("auth").toString()
                                     .replace("[", "")
                                     .replace("]", "")
                                     .split(", "))
                             .map(SimpleGrantedAuthority::new)
                             .collect(Collectors.toList());
        }

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    // Token에서 Claims 추출
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // Token에서 사용자의 id(subject 클레임) 추출
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }


}
