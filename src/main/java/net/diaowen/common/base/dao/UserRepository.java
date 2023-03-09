package net.diaowen.common.base.dao;

import net.diaowen.common.base.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * user repository
 *
 * @author lance
 * @since 2023/3/9 11:37
 */
public interface UserRepository extends JpaRepository<User, String> {
}
