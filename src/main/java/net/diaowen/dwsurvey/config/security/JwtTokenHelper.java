package net.diaowen.dwsurvey.config.security;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.dwsurvey.config.prop.SurveyProperties;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

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

  /**
   * 生成token
   *
   * @param authentication Authentication
   * @return String
   */
  public String generateJwtToken(Authentication authentication) {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    return Jwts.builder()
        .setSubject((userPrincipal.getUsername()))
        .claim("userId", userPrincipal.getId())
        .claim("email", userPrincipal.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + surveyProperties.getJwt().getExpiration()))
        .signWith(SignatureAlgorithm.HS512, surveyProperties.getJwt().getSecret())
        .compact();
  }

  /**
   * 获取username
   *
   * @param token token
   * @return username
   */
  public String getUsername(String token) {
    Claims claims = getClaims(token);
    return claims.getSubject();
  }

  /**
   * 获取userId
   *
   * @param token token
   * @return userId
   */
  public String getUserId(String token) {
    Claims claims = getClaims(token);
    return claims.get("userId", String.class);
  }

  /**
   * 获取email
   *
   * @param token token
   * @return email
   */
  public String getEmail(String token) {
    Claims claims = getClaims(token);
    return claims.get("email", String.class);
  }

  private Claims getClaims(String token) {
    return Jwts.parser().setSigningKey(surveyProperties.getJwt().getSecret())
        .parseClaimsJws(token)
        .getBody();
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
