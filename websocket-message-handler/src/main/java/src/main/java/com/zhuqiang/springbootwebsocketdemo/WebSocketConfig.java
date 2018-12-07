package src.main.java.com.zhuqiang.springbootwebsocketdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

import java.util.List;

/**
 * WebSocketConfig用@Configuration标注，以表明它是一个Spring配置类。
 * 它也被标注为@EnableWebSocketMessageBroker。
 * 顾名思义，@EnableWebSocketMessageBroker启用由消息代理支持的WebSocket消息处理。
 *
 * @author qiangzhu4
 * @create 2018-12-06 13:15
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends WebSocketMessageBrokerConfigurationSupport implements WebSocketMessageBrokerConfigurer {

    /**
     * 获取一个WebSocketHandler
     *
     * @return WebSocketHandler
     */
    @Bean
    public WebSocketHandler subProtocolWebSocketHandler() {
        return new OASubProtocolWebSocketHandler(clientInboundChannel(),
                clientOutboundChannel());
    }


    /**
     * 启用一个简单的message broker并配置一个或多个前缀进行筛选
     * <br>
     * configureMessageBroker()方法实现websocketmessagebrokerer配置器中的默认方法来配置消息代理。
     * 它首先调用enableSimpleBroker()来启用一个简单的基于内存的消息代理，以便在以“/topic”为前缀的目的地将问候消息带回客户机。
     * 它还为绑定到@ messagemapping-annotedmethods的消息指定“/app”前缀。
     * 这个前缀将用于定义所有消息映射。
     *
     * @param config 用于配置message broker选项的注册中心
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //启用一个简单的message broker并配置一个或多个前缀进行筛选,即客户端订阅的消息必须包含/topic前缀，才能收到消息
        config.enableSimpleBroker("/topic");
        //配置一个或多个前缀来筛选目标应用程序，服务端只处理/app为前缀的消息。客户端发送的消息路径必须包含/app
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 注册每个STOMP端点映射到一个特定的URL和(可选)启用和配置SockJS回退选项。
     * <li>addEndpoint:方法注册“/ws”端点</li>
     * <li>启用SockJS回退选项，以便在WebSocket不可用时可以使用替代传输</li>
     * <li>握手拦截器设置</li>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws/")
                .withSockJS().setInterceptors(new WebsocketInterceptor());
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration webSocketTransportRegistration) {
        super.configureWebSocketTransport(webSocketTransportRegistration);
        webSocketTransportRegistration.setMessageSizeLimit(64 * 1024); // default : 64 * 1024
        webSocketTransportRegistration.setSendTimeLimit(20 * 10000); // default : 10 * 10000
        webSocketTransportRegistration.setSendBufferSizeLimit(3 * 512 * 1024); // default : 512 * 1024

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {

    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {

    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {

    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        return false;
    }
}