package br.ufrgs.inf.pet.dinoapi.websocket.config;

import br.ufrgs.inf.pet.dinoapi.config.AppOriginConfig;
import br.ufrgs.inf.pet.dinoapi.websocket.interceptor.WebSocketUserInterceptor;
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
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        final String origin = new AppOriginConfig().getOrigin();
        registry
                .addEndpoint("/websocket")
                .setHandshakeHandler(new WebSocketUserInterceptor())
                .setAllowedOrigins(origin)
                .withSockJS();
    }
}
