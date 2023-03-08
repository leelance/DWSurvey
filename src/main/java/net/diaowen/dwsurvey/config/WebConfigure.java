package net.diaowen.dwsurvey.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 项目启动
 *
 * @author diaowen
 * @since 2023/3/8 23:59
 */
@Configuration
public class WebConfigure implements WebMvcConfigurer {

  @Bean
  public FilterRegistrationBean<OpenEntityManagerInViewFilter> registerOpenEntityManagerInViewFilterBean() {
    FilterRegistrationBean<OpenEntityManagerInViewFilter> registrationBean = new FilterRegistrationBean<>();
    OpenEntityManagerInViewFilter filter = new OpenEntityManagerInViewFilter();
    registrationBean.setFilter(filter);
    registrationBean.addUrlPatterns("*.do");
    return registrationBean;
  }
}
