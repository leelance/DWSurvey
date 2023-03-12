package net.diaowen.dwsurvey.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 定义属性
 *
 * @author lance
 * @since 2023/3/9 10:11
 */
@Data
@Component
@ConfigurationProperties(prefix = SurveyProperties.PREFIX)
public class SurveyProperties {
  public static final String PREFIX = "dwsurvey";

  /**
   * 用户模式切换，暂且保持默认 local
   */
  private String site = "local";

  private JwtProperties jwt;

  private Version version;

  @Data
  public static class JwtProperties {
    /**
     * jwt加密key
     */
    private String secret;
    /**
     * jwt有效期
     */
    private long expiration;
  }

  @Data
  public static class Version {
    private String info;
    private String number;
    private String built;
  }
}
