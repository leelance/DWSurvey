package net.diaowen.dwsurvey.service;

import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.json.JsonUtils;
import net.diaowen.common.utils.StringConst;
import net.diaowen.dwsurvey.entity.SurveyDirectory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SurveyDirectoryManager
 *
 * @author lance
 * @since 2023/3/9 15:42
 */
@Slf4j
@SpringBootTest
class SurveyDirectoryManagerTests {
  @Autowired
  private SurveyDirectoryManager surveyDirectoryManager;
  @Autowired
  private AuthenticationManager authenticationManager;

  @BeforeEach
  void init() {
    String userName = "dwsurvey";
    String password = "123456";
    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  void findByUser() {
    PageRequest page = PageRequest.of(0, 10);
    String surveyName = StringConst.EMPTY;
    Integer surveyState = null;

    Page<SurveyDirectory> result = surveyDirectoryManager.findByUser(page, surveyName, surveyState);
    log.info("===>{}", JsonUtils.toJsonString(result));
    log.info("===>{}", result.getTotalElements());
    Assertions.assertNotNull(result);
  }
}