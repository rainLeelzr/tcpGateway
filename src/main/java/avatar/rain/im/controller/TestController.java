package avatar.rain.im.controller;

import avatar.rain.core.Menu;
import avatar.rain.core.User;
import avatar.rain.core.net.atcp.request.RequestCmd;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/hello")
    @RequestCmd(cmd = 1, protobuf = "avatar.rain.im.protobuf.IM$SendTextToUserC2S")
    public String hello(int toUserId, String message, User user, Menu menu) {
        System.out.println("toUserId=" + toUserId);
        System.out.println("message=" + message);
        System.out.println("user=" + user);
        System.out.println("menu=" + menu);
        System.out.println("imServer: /test/hello");
        return "imServer: /test/hello";
    }
}
