package net.diaowen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 项目启动
 *
 * @author diaowen
 * @since 2023/3/8 23:59
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = {"net.diaowen.common.base.dao", "net.diaowen.common.dao"})
public class SurveyApplication {

  public static void main(String[] args) {
    SpringApplication.run(SurveyApplication.class, args);
  }

}
