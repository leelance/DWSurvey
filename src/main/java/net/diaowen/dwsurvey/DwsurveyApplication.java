package net.diaowen.dwsurvey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 项目启动
 *
 * @author diaowen
 * @since 2023/3/8 23:59
 */
@SpringBootApplication
@ComponentScan(basePackages = {"net.diaowen.common", "net.diaowen.dwsurvey"})
public class DwsurveyApplication {

  public static void main(String[] args) {
    SpringApplication.run(DwsurveyApplication.class, args);
  }

}
