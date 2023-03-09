package net.diaowen.dwsurvey.config;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

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
  public CorsFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOriginPatterns(Collections.singletonList("*"));
    corsConfig.setMaxAge(3600L);
    corsConfig.setAllowedMethods(Lists.newArrayList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", corsConfig);
    return new CorsFilter(source);
  }

}
