package net.diaowen.common.base.dao;

import net.diaowen.common.base.entity.User;
import net.diaowen.common.dao.BaseDaoImpl;
import org.springframework.stereotype.Repository;


/**
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Repository
public class UserDaoImpl extends BaseDaoImpl<User, String> implements UserDao {

  @Override
  public void resetUserGroup(String groupId) {
    String sql = "UPDATE t_user SET user_group_id = '' WHERE id = id";
    this.getSession().createNativeQuery(sql).executeUpdate();
  }
}
