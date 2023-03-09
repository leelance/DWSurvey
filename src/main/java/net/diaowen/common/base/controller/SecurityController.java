package net.diaowen.common.base.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.dwsurvey.config.security.JwtTokenHelper;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
