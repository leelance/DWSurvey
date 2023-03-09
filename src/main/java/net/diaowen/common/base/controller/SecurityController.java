package net.diaowen.common.base.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.plugs.httpclient.HttpResult;
import net.diaowen.dwsurvey.config.security.JwtTokenHelper;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * user login / logout
 *
 * @author lance
 * @since 2023/3/9 09:41
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/survey/anon/security")
public class SecurityController {
  private final AuthenticationManager authenticationManager;
  private final JwtTokenHelper jwtTokenHelper;

  /**
   * 用户登陆接口
   *
   * @param userName userName
   * @param password password
   * @return LoginRegisterResult
   */
  @PostMapping("/login.do")
  public LoginRegisterResult login(String userName, String password) {
    if (log.isDebugEnabled()) {
      log.debug("===>login username: {}, password: {}", userName, password);
    }
    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String token = jwtTokenHelper.generateJwtToken(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    List<String> roles = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
    return LoginRegisterResult.success(roles, token);
  }

  @PostMapping("/logout.do")
  public HttpResult<String> logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      if (log.isDebugEnabled()) {
        log.debug("===>{} logout", auth.getPrincipal());
      }
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
    return HttpResult.success();
  }
}
