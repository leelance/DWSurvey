package net.diaowen.dwsurvey.config.security;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.dwsurvey.config.prop.SurveyProperties;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt token helper
 *
 * @author lance
 * @since 2023/3/9 01:07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenHelper {
  private final SurveyProperties surveyProperties;

  public String generateJwtToken(Authentication authentication) {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
    Map<String, Object> map = new HashMap<>(8);
    map.put("userId", userPrincipal.getId());
    map.put("email", userPrincipal.getEmail());
    return Jwts.builder()
        .setSubject((userPrincipal.getUsername()))
        .setClaims(map)
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + surveyProperties.getJwt().getExpiration()))
        .signWith(SignatureAlgorithm.HS512, surveyProperties.getJwt().getSecret())
        .compact();
  }

  public String getUserNameFromJwtToken(String token) {
    return Jwts.parser().setSigningKey(surveyProperties.getJwt().getSecret())
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(surveyProperties.getJwt().getSecret()).parseClaimsJws(authToken);
      return true;
    } catch (MalformedJwtException e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.warn("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.warn("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.warn("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }
}
