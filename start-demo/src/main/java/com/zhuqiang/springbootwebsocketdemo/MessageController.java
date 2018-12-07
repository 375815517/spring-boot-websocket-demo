package com.zhuqiang.springbootwebsocketdemo;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

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
