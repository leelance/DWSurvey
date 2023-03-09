package net.diaowen.common.base.controller;

import lombok.Data;
import net.diaowen.common.plugs.httpclient.HttpResult;

import java.util.List;
import java.util.Objects;

/**
 * LoginRegisterResult
 *
 * @author diaowen
 * @since 2023/3/9 10:26
 */
@Data
public class LoginRegisterResult {
  private String status;
  private String type = "account";
  private String[] currentAuthority;
  private String token;
  private HttpResult httpResult;


  public static LoginRegisterResult RESULT(String status, String type) {
    LoginRegisterResult loginResult = new LoginRegisterResult();
    loginResult.setStatus(status);
    loginResult.setType(type);
    loginResult.setCurrentAuthority(new String[]{});
    return loginResult;
  }

  public static LoginRegisterResult SUCCESS(String currentAuthority) {
    LoginRegisterResult loginResult = new LoginRegisterResult();
    loginResult.setStatus("ok");
    loginResult.setCurrentAuthority(new String[]{currentAuthority});
    return loginResult;
  }

  public static LoginRegisterResult success(List<String> currentAuthority, String token) {
    LoginRegisterResult loginResult = new LoginRegisterResult();
    loginResult.setStatus("ok");
    if (Objects.nonNull(currentAuthority) && !currentAuthority.isEmpty()) {
      loginResult.setCurrentAuthority(currentAuthority.toArray(new String[0]));
    } else {
      loginResult.setCurrentAuthority(new String[0]);
    }
    loginResult.setToken(token);
    return loginResult;
  }

  public static LoginRegisterResult FAILURE() {
    LoginRegisterResult loginResult = new LoginRegisterResult();
    loginResult.setStatus("error");
    loginResult.setCurrentAuthority(new String[]{"guest"});
    return loginResult;
  }

  public static LoginRegisterResult FAILURE(HttpResult httpResult) {
    LoginRegisterResult loginResult = new LoginRegisterResult();
    loginResult.setStatus("error");
    loginResult.setCurrentAuthority(new String[]{"guest"});
    loginResult.setHttpResult(httpResult);
    return loginResult;
  }
}
