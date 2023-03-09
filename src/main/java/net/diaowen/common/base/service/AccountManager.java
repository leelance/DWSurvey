package net.diaowen.common.base.service;

import lombok.RequiredArgsConstructor;
import net.diaowen.common.base.dao.UserDao;
import net.diaowen.common.base.entity.User;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author KeYuan
 * @since 2013下午10:22:04
 */
@Service
@RequiredArgsConstructor
public class AccountManager {
  private final UserDao userDao;


  public void saveUp(User user) {
    userDao.save(user);
  }

  @Transactional(readOnly = true)
  public User findUserByLoginName(String loginName) {
    return userDao.findUniqueBy("email", loginName);
  }

  /**
   * 取出当前登陆用户
   */
  public UserDetailsImpl getCurUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof UserDetailsImpl) {
      return (UserDetailsImpl) principal;
    }

    return null;
  }
}
