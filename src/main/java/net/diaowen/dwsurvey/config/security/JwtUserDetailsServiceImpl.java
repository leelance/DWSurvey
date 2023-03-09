package net.diaowen.dwsurvey.config.security;

import lombok.RequiredArgsConstructor;
import net.diaowen.common.base.entity.User;
import net.diaowen.common.base.service.AccountManager;
import net.diaowen.dwsurvey.common.enums.UserStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

/**
 * jwt user detail
 *
 * @author lance
 * @since 2023/3/9 01:09
 */
@Component
@RequiredArgsConstructor
public class JwtUserDetailsServiceImpl implements UserDetailsService {
  protected final AccountManager accountManager;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = accountManager.findUserByLoginName(username);
    if (Objects.isNull(user)) {
      throw new UsernameNotFoundException("用户不存在或者密码错误");
    }

    //2.激活 1.未激活 0.不可用
    int status = user.getStatus();
    if (status == UserStatus.NO.getCode()) {
      throw new LockedException("当前用户已锁定");
    }

    if (status == UserStatus.INACTIVE.getCode()) {
      throw new LockedException("当前用户尚未激活");
    }
    return new UserDetailsImpl(user.getId(), username, user.getEmail(), user.getShaPassword(), new ArrayList<>());
  }
}
