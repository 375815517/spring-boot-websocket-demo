##### 握手拦截
- author zhuqiang
- email 375815517@qq.com
- gitpath https://github.com/375815517/spring-boot-websocket-demo.git


websocket 在建立连接之前需要进行握手操作。握手操作是通过http请求进行的。当websocket连接确认之后websocket连接才正式建立。其后将按照websocket通讯协议进行通讯。在此之前，其握手行为遵守的是http协议。websocket在握手时可以传递一些属性。可以通过拦截握手请求获取连接前的参数属性，进行相关操作。比如token认证。(次文档在spring boot websocket demo基础上进行撰写)项目的源码存放于github中（websocket-handshake-interceptor模块）。


###### spring boot websocket握手拦截

- 拦截接口HandshakeInterceptor
基于spring boot 框架下的握手请求拦截需要实现一个核心接口HandshakeInterceptor。该接口定义了握手请求前的操作和握手请求后的操作。
```
用于WebSocket握手请求的拦截器。
可以用来检查握手请求和响应以及传递属性给目标
public interface HandshakeInterceptor {

	/**
	 * 该方法在握手进行前被调用.
	 * @param request       当前的握手请求对象
	 * @param response      当前的握手响应对象
	 * @param wsHandler     websocket handler
	 * @param attributes    一个空的map,在该方法中将属性存放至该map,则属性会一直保存在websocket session中。
	 * @return 是否进行握手操作
	 */
	boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception;

	/**
	 * 握手完成后被调用.响应结果集握手结果将存放在response中。同时在handler中将可以看到用户传递的属性
	 * @param request the current request
	 * @param response the current response
	 * @param wsHandler the target WebSocket handler
	 * @param exception an exception raised during the handshake, or {@code null} if none
	 */
	void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, @Nullable Exception exception);

}
```
- 实现拦截接口

 根据接口协议，实现一个拦截器。改拦截器将握手请求的url后面参数获取，放置到属性当中，详细的代码参考下面。

```
/**
 * websocket握手拦截
 *
 * @author qiangzhu4
 * @create 2018-11-28 9:56
 **/
@Component
public class WebsocketInterceptor implements HandshakeInterceptor {

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(WebsocketInterceptor.class);
    /**
     * http session id constant
     */
    private static final String HTTP_SESSION_ID_ATTR_NAME = "HTTP.SESSION.ID";


    /**
     * 获取http session
     *
     * @param request http请求
     * @return http session
     */
    private HttpSession getSession(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
            return serverRequest.getServletRequest().getSession();
        }
        return null;
    }

    /**
     * websocket 进行握手时将执行此方法，本方法将握手前的请求信息及相关属性放入指定参数中
     * @param request request
     * @param response response
     * @param wsHandler wsHandler
     * @param attributes attributes
     * @return 数据处理结果
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        //获取http session，若http session 不为空，将session中属性信息存入attributes
        HttpSession session = getSession(request);
        if (session != null) {
            attributes.put(HTTP_SESSION_ID_ATTR_NAME, session.getId());
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                attributes.put(name, session.getAttribute(name));
            }
        }
        request.getHeaders().keySet().forEach(v -> attributes.put(v, request.getHeaders().get(v)));
        attributes.putAll(getParameters(request.getURI().toString()));
        return true;
    }

    /**
     * 获取请求头中的参数
     *
     * @param url url
     * @return 参数列表
     */
    private Map<String, Object> getParameters(String url) {
        LOG.info("开始解析url链接参数：url={}", url);
        Map<String, Object> map = new HashMap<>();
        try {
            if (ObjectUtils.isEmpty(url)) return map;
            String[] sp = url.split("[?]");
            if (ObjectUtils.isEmpty(sp) || sp.length != 2 || ObjectUtils.isEmpty(sp[1])) return map;
            String[] paramStr = sp[1].split("&");
            for (String s : paramStr) {
                String[] kv = s.split("=");
                if (!ObjectUtils.isEmpty(kv) && !ObjectUtils.isEmpty(kv[0]) && !ObjectUtils.isEmpty(kv[1]))
                    map.put(kv[0], kv[1]);
            }
        } catch (Exception e) {
            LOG.error("握手连接解析失败：url={},e={}", url, e);
        }
        return map;
    }

    /**
     * 握手后回调
     *:websocket 握手协议执行完毕后将回调词方法
     * @param request request
     * @param response response
     * @param wsHandler wsHandler
     * @param  exception exception
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler
            wsHandler, Exception exception) {
    }
}

```
###### 配置拦截器
websocket拦截器配置完成后需要在配置文件中配置拦截器对象。在实现WebSocketMessageBrokerConfigurer的配置类WebSocketConfig的protected abstract void registerStompEndpoints(StompEndpointRegistry registry);方法中设置。

```
    /**
     * 注册每个STOMP端点映射到一个特定的URL和(可选)启用和配置SockJS回退选项。
     * <li>addEndpoint:方法注册“/ws”端点</li>
     * <li>启用SockJS回退选项，以便在WebSocket不可用时可以使用替代传输</li>
     * <li>握手拦截器设置</li>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
   
        registry.addEndpoint("/ws/")
                .withSockJS().setInterceptors(websocketInterceptor);
    }
```

###### 属性传递
需要传递参数时仅需要在建立websocket连接时在url上拼接参数即可如：
```
function connect() {
    var socket = new SockJS('/ws?qq=375815517');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/response', function (greeting) {
            showGreeting(greeting.body);
        });
    });
}
```
