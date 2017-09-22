package avatar.rain.gateway.service;

import avatar.rain.auth.entity.User;
import avatar.rain.core.api.ServerApi;
import avatar.rain.core.util.log.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TcpToHttpService  {

    @Resource
    private RestTemplate restTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 各个微服务提供可以进行tcp访问的api集合
     * key:   微服务的名称
     * value: {key: urlMapping，value: Api}
     */
    private Map<String, ServerApi> microServerApis = new ConcurrentHashMap<>();

    public String transmit(Integer toUserId, String message, User user) {
        LogUtil.getLogger().debug("tcpGateway: /test/hello");
        return "tcpGateway: /test/hello";
    }


}
