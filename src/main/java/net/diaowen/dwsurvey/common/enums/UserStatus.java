package net.diaowen.dwsurvey.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 2.激活 1.未激活 0.不可用
 *
 * @author lance
 * @since 2023/3/9 01:17
 */
@Getter
@AllArgsConstructor
public enum UserStatus {
  /**
   * 状态有效无效
   */
  NO(0, "不可用"),
  INACTIVE(1, "未激活"),
  ACTIVE(2, "正常"),
  ;

  private final int code;
  private final String name;
}