package src.main.java.com.zhuqiang.springbootwebsocketdemo;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

/**
 * @author qiangzhu4
 * @create 2018-12-07 16:16
 **/
public class OASubProtocolWebSocketHandler extends SubProtocolWebSocketHandler {


    /**
     * 默认构造方法
     * Create a new {@code SubProtocolWebSocketHandler} for the given inbound and outbound channels.
     *
     * @param clientInboundChannel  the inbound {@code MessageChannel}
     * @param clientOutboundChannel the outbound {@code MessageChannel}
     */
    OASubProtocolWebSocketHandler(MessageChannel clientInboundChannel, SubscribableChannel clientOutboundChannel) {
        super(clientInboundChannel, clientOutboundChannel);
    }

    /**
     * websocket连接确认
     *
     * @param session websocket session
     * @throws Exception ex
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session)
            throws Exception {
        System.out.println("websocket 连接");
        super.afterConnectionEstablished(session);
    }

    /**
     * 消息处理
     *
     * @param session session
     * @param message message
     * @throws Exception 可能存在的异常
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        System.out.println("收到消息：" + message.toString());
        //继承父类
        super.handleMessage(session, message);

    }

    /**
     * websocket 连接断开
     *
     * @param wss wss
     * @param cs  cs
     * @throws Exception ex
     */
    @Override
    public void afterConnectionClosed(WebSocketSession wss, CloseStatus cs) throws Exception {
        System.out.println("websocket 连接断开");
        super.afterConnectionClosed(wss, cs);
    }
}
