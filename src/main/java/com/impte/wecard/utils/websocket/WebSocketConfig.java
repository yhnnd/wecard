package com.impte.wecard.utils.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurationSupport implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 允许连接的域,只能以http或https开头
        // String[] allowsOrigins = {"http://www.xxx.com"};
        // WebIM WebSocket通道
        // 大坑，跨域改成setAllowedOrigins("*")，否则只能是www.xxx.com访问
        registry.addHandler(WebSocket(),"/webSocketIMServer")
                .setAllowedOrigins("*")
                .addInterceptors(myInterceptor());

        registry.addHandler(WebSocket(), "/sockjs/webSocketIMServer")
                .setAllowedOrigins("*")
                .addInterceptors(myInterceptor())
                .withSockJS();
    }

    @Bean
    public WebSocket WebSocket() {  return new WebSocket(); }
    @Bean
    public HandshakeInterceptor myInterceptor(){ return new HandshakeInterceptor(); }
}