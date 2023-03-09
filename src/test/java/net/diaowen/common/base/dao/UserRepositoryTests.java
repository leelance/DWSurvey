package net.diaowen.common.base.dao;

import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.base.entity.User;
import net.diaowen.common.json.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * user repository
 *
 * @author lance
 * @since 2023/3/9 11:37
 */
@Slf4j
@SpringBootTest
class UserRepositoryTests {
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  @Disabled("updatePassword")
  void updatePassword() {
    String userId = "1";
    Optional<User> optional = userRepository.findById(userId);

    optional.ifPresent(user -> {
      log.info("===>user: {}", JsonUtils.toJsonString(user));
      user.setShaPassword(passwordEncoder.encode("123456"));

      userRepository.saveAndFlush(user);
    });
    Assertions.assertNotNull(optional);
  }
}