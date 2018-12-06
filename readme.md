##### 开始之前
- author zhuqiang
- email 375815517@qq.com

WebSocket是TCP之上的一个非常薄的轻量级层。它非常适合使用“子协议”来嵌入消息。在本文中，我们将深入研究如何将STOMP消息与Spring一起创建交互式web应用程序。项目的源码存放于github中。

###### 项目依赖
-  maven
- spring boot 
- idea 编译器
- jdk1.8

###### 创建一个spring boot 项目
在进入正题之前通过idea 编辑器创建一个基于maven 的spring boot项目。此处需要做三件事情。引入合适的mavne 依赖包。添加maven build插件。创建spring boot 启动类。

- spring boot maven 依赖
项目创建后需引入基本的maven依赖。
```
 <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
```

- maven build 插件
```
 <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

- spring boot 启动类
```
@SpringBootApplication
public class SpringBootWebsocketDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebsocketDemoApplication.class, args);
    }
}
```

##### 创建一个简单的websocket程序
基于maven 的spring boot程序创建完成后就可以正式创建一个websocket应用程序了。

###### 引入spring boot websocket maven包
```
 <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
```

###### 创建WebSocketConfig

实际上下面代码注释已经对其作用进行了说明。这里进行分布阐述。

- 创建并注入一个websocket消息代理类
实现WebSocketMessageBrokerConfigurer接口。并将其注入spring中。spring boot将自动启动websocket代理，处理websocket消息。
在类头中加入@Configuration          @EnableWebSocketMessageBroker注解即可实现该功能。

- 实现 configureMessageBroker(MessageBrokerRegistry config)方法

方法的主要作用是配置message broker选项。这里我们至少配置2个选项。

1.客户端订阅约束，即客户端的订阅地址必须包含指定前缀开头，服务端才能处理。配置方法为：

config.enableSimpleBroker("/topic")，表明用户订阅请求必须以/topic开头。

2.服务端处理约束，改配置是针对服务端的，服务端收到的消息路径必然是以某些指定前缀开头的。配置方法：

config.setApplicationDestinationPrefixes("/app");改配置表明服务端处理的消息的路径必然以/app前缀。

- 实现registerStompEndpoints(StompEndpointRegistry registry)方法

此方法的作用是：注册每个STOMP端点映射到一个特定的URL和(可选)启用和配置SockJS回退选项。这里拆分为2点说明。

1.指定一个断言映射到一个特点url,即指定了websokcet连接的地址，客户端端建立websocket连接必须以指定的地址建立连接。我们定义连接的地址为/ws，因此可以这样设置：registry.addEndpoint("/ws")

2.指定启用SockJS回退选项，以便在WebSocket不可用时可以使用替代传输数据。这里使用方法：withSockJS()

至此一个最简单的websocket配置类完成。

```
/**
 * WebSocketConfig用@Configuration标注，以表明它是一个Spring配置类。
 * 它也被标注为@EnableWebSocketMessageBroker。
 * 顾名思义，@EnableWebSocketMessageBroker启用由消息代理支持的WebSocket消息处理。
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 启用一个简单的message broker并配置一个或多个前缀进行筛选
     *<br>
     * configureMessageBroker()方法实现websocketmessagebrokerer配置器中的默认方法来配置消息代理。
     * 它首先调用enableSimpleBroker()来启用一个简单的基于内存的消息代理，以便在以“/topic”为前缀的目的地将问候消息带回客户机。
     * 它还为绑定到@ messagemapping-annotedmethods的消息指定“/app”前缀。
     * 这个前缀将用于定义所有消息映射。
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
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /*
         * <li>addEndpoint:方法注册“/ws”端点</li>
         * <li>启用SockJS回退选项，以便在WebSocket不可用时可以使用替代传输</li>
         */
        registry.addEndpoint("/ws")
                .withSockJS();
    }

}
```

###### 服务端消息的接收与发送
基于spring boot的websocket服务端数据接收与发送非常简单。只需要在controller层添加注解即可。
- 接收消息

通过注解@MessageMapping即可接收指定路径发来的消息。如 @MessageMapping("/message")接收/app/message 路径的消息。之所以省去/app前缀是因为配置中添加了前缀过滤，因此可以省略。注解应当注到方法上，方法的参数即为我们接受的参数


- 发送消息

通过注解@SendTo即可将消息发送至指定的路径上去，如@SendTo("/topic/response")即可将消息发送至订阅了/topic/response路径的客户端。注解应注到指定方法上，方法的返回值即为要发送的数据。

- 注意

该注解应当用在spring 的controller层，及方法所在的类应当被@Controller注解注入至spring

```
/**
 * websocket message controller
 *
 * @author qiangzhu4
 * @create 2018-12-06 14:15
 **/
@Controller
public class MessageController {

    @MessageMapping("/message")
    @SendTo("/topic/response")
    public String onMessage(String message) throws Exception {
        Thread.sleep(1000); // simulated delay
        System.out.println(message);
        return "receive ：" + message;
    }
}

```

###### 客户端消息的接收与发送
这里客户端我们使用js+html。
- maven引入

这些包为前端的一些依赖。
```
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>webjars-locator-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>sockjs-client</artifactId>
            <version>1.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>stomp-websocket</artifactId>
            <version>2.3.3</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>bootstrap</artifactId>
            <version>3.3.7</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>jquery</artifactId>
            <version>3.1.0</version>
        </dependency>
```

- 前端代码-js

使用socketJS建立连接。连接地址必须为服务端配置的指定地址即http://host:port/path。或简写为path.我们服务端配置的path为/ws
```
   var socket = new SockJS('/ws/');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
    });
```

- 前端订阅

订阅路径必须为服务端指定前缀，这里服务端指定前缀为/topic
```
        stompClient.subscribe('/topic/response', function (greeting) {
            showGreeting(greeting.body);
        });
```
- 服务端推送

服务端推送地址必须为指定前缀地址，这里服务端设置的为/app
```
stompClient.send("/app/message", {}, $("#name").val());
```






