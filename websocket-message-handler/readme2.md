##### websocket 消息大小限制

- author zhuqiang
- email 375815517@qq.com
- gitpath https://github.com/375815517/spring-boot-websocket-demo.git

spring websocket在通讯过程可能因为消息过大导致处理失败。但其提供了消息大小限制的设置方法。在创建websocket配置类时。重写 public void configureWebSocketTransport(WebSocketTransportRegistration webSocketTransportRegistration)方法。此方法为WebSocketMessageBrokerConfigurationSupport类中的一个方法，不过该方法为一个空方法。未实现任何逻辑。继承并实现即可限制消息大小。项目的源码存放于github中（websocket-message-handler模块）。

```
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration webSocketTransportRegistration) {
        super.configureWebSocketTransport(webSocketTransportRegistration);
        webSocketTransportRegistration.setMessageSizeLimit(64 * 1024); // default : 64 * 1024
        webSocketTransportRegistration.setSendTimeLimit(20 * 10000); // default : 10 * 10000
        webSocketTransportRegistration.setSendBufferSizeLimit(3 * 512 * 1024); // default : 512 * 1024

    }
```