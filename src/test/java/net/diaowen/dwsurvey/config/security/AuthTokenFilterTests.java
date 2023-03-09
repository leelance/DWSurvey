package net.diaowen.dwsurvey.config.security;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * 测试token生成和校验
 *
 * @author lance
 * @since 2023/3/9 13:54
 */
@Slf4j
@SpringBootTest
class AuthTokenFilterTests {
  @Autowired
  private JwtTokenHelper jwtTokenHelper;
  @Autowired
  private AuthenticationManager authenticationManager;

  @Test
  @Disabled("doFilterInternal")
  void doFilterInternal() {
    String userName = "dwsurvey";
    String password = "123456";

    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
    String token = jwtTokenHelper.generateJwtToken(authentication);
    log.info("===>token: {}", token);
    Assertions.assertNotNull(token);

    String sub = jwtTokenHelper.getUsername(token);
    log.info("===>sub: {}", sub);

    String userId = jwtTokenHelper.getUserId(token);
    log.info("===>userId: {}", userId);

    String email = jwtTokenHelper.getEmail(token);
    log.info("===>email: {}", email);

    boolean isValid = jwtTokenHelper.validateJwtToken(token);
    log.info("===>isValid: {}", isValid);
    Assertions.assertTrue(isValid);
  }
}