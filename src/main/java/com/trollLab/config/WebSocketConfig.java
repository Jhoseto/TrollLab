package com.trollLab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Настройване само на индивидуални канали за всеки потребител
        config.enableSimpleBroker("/user"); // Поддържаме само индивидуални канали за потребители

        // Префикс за изпращане на съобщения към сървъра
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Регистрация на WebSocket endpoint
        registry.addEndpoint("/ws").withSockJS(); // Това е крайният точка за WebSocket
    }
}
