package avatar.rain;

import avatar.rain.core.api.ApiManager;
import avatar.rain.core.net.atcp.server.TcpServer;
import avatar.rain.core.serialization.ProtobufSerializationManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AvatarApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private ApiManager apiManager;

    @Resource
    private ProtobufSerializationManager protobufSerializationManager;

    @Resource
    private TcpServer tcpServer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        apiManager.init();
        protobufSerializationManager.init();
        new Thread("netty-starter") {

            @Override
            public void run() {
                tcpServer.start();
            }
        }.start();
    }

}
