package src.main.java.com.zhuqiang.springbootwebsocketdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
        LOG.info("连接确认");
    }
}
