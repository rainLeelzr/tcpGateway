package avatar.rain.im.controller;

import avatar.rain.auth.entity.User;
import avatar.rain.core.net.tcp.request.RequestCmd;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/hello")
    @RequestCmd(url = "/test/hello", protobuf = "avatar.rain.im.protobuf.IM$SendTextToUserC2S")
    public String hello(Integer toUserId, String message, User user) {
        System.out.println("tcpGateway: /test/hello");
        return "tcpGateway: /test/hello";
    }
}
