package com.impte.wecard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfigurer implements WebMvcConfigurer {

  @Autowired
  private LoginInterceptor loginInterceptor;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    //指定静态目录位置
    registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
    registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/");
    registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/");
    registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("index");
    registry.addViewController("/login.html").setViewName("login");
    registry.addViewController("/index.html").setViewName("index");
    registry.addViewController("/forgot-password.html").setViewName("forgot-password");
    registry.addViewController("/signup.html").setViewName("signup");
    registry.addViewController("/card-page.html").setViewName("card-page");
    registry.addViewController("/user-page.html").setViewName("user-page");
    registry.addViewController("/room-page.html").setViewName("room-page");
    registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
  }

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/login/**", "/register/**", "/account/isLogin",
            "/card/load", "/card/read", "/card/search",
            "/", "/*.html", "/js/**", "/css/*", "/img/*", "/fonts/*",
            "/error", "/find/message", "/qqLogin", "/qqConnect",
            "/find/simple/user", "/find/simple/room"
        );
  }
}
