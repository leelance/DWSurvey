package net.diaowen.dwsurvey.config.security;

import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.json.JsonUtils;
import net.diaowen.common.plugs.httpclient.HttpResult;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * AuthEntryPointJwt
 *
 * @author lance
 * @since 2023/3/9 01:30
 */
@Slf4j
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
    log.warn("Unauthorized[{}] fail: ", request.getRequestURI(), exception);

    String message;
    if (exception instanceof BadCredentialsException) {
      message = "用户名或密码错误！";
    } else if (exception instanceof LockedException) {
      message = "用户已被锁定！";
    } else if (exception instanceof InsufficientAuthenticationException) {
      message = "token无效";
    } else {
      message = "认证失败，请联系网站管理员！";
    }

    HttpResult<String> result = new HttpResult<>(HttpServletResponse.SC_UNAUTHORIZED);
    result.setResultMsg(message);

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.getWriter().write(JsonUtils.toJsonString(result));
    response.getWriter().flush();
  }
}