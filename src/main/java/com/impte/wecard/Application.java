package com.impte.wecard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages = "com.impte.wecard")
@MapperScan("com.impte.wecard.dao")
@EntityScan("com.impte.wecard.domain.po")
@PropertySource({"classpath:qqconnectconfig.properties", "classpath:sys.properties"})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
