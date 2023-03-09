package net.diaowen.dwsurvey.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * HttpClientConfig
 *
 * @author diaowen
 * @since 2023/3/9 19:30
 */
@Configuration
@ImportResource(locations = {"classpath:conf/httpclient/applicationContext-httpclient.xml"})
public class HttpClientConfig {

  @Bean
  public WebMvcConfigurer corsMappingConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns()
            .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD")
            .maxAge(3600);
      }
    };
  }

}
