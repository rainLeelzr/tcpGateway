package avatar.rain.gateway.service;

import avatar.rain.core.api.Api;
import avatar.rain.core.api.MicroServerApi;
import avatar.rain.core.api.ServerApi;
import avatar.rain.core.util.log.LogUtil;
import avatar.rain.result.Result;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MicroServerService implements MicroServerApi, InitializingBean {

    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private RestTemplate noBalanceRestTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 各个微服务提供可以进行tcp访问的api集合
     * key:   微服务的名称
     * value: {key: urlMapping，value: Api}
     */
    private Map<String, ServerApi> microServerApis = new ConcurrentHashMap<>();

    public ServerApi getMicroServerByServerName(String serverName) {
        return microServerApis.get(serverName);
    }

    /**
     * 更新在Eureka上注册了的可以提供tcp的服务
     */
    @Scheduled(initialDelay = 1000 * 30, fixedRate = 1000 * 30)
    @SuppressWarnings("unchecked")
    public void updateEurekaTcpServer() {
        List<String> services = discoveryClient.getServices();
        // 更新成功的个数
        int updateCount = 0;
        for (String serverName : services) {
            if (serverName.equalsIgnoreCase(applicationName)) {
                continue;
            }

            List<ServiceInstance> instances = discoveryClient.getInstances(serverName);

            if (instances.isEmpty()) {
                continue;
            }

            // 在此serverName的服务集群下，最后启动的那个服务的api初始化时间
            long lastInitTime = 0;

            // 在此serverName的服务集群下，最后启动的那个服务的下标
            int index = -1;

            for (int i = 0; i < instances.size(); i++) {
                try {
                    String initTimeUrl = instances.get(i).getUri() + "/api/initTime";
                    long initTime = noBalanceRestTemplate.getForObject(initTimeUrl, long.class);

                    ServerApi serverApi = microServerApis.get(serverName);
                    if (serverApi == null) {
                        if (initTime > lastInitTime) {
                            lastInitTime = initTime;
                            index = i;
                        }
                    } else if (initTime > serverApi.getTime()) {
                        lastInitTime = initTime;
                        index = i;
                    }
                } catch (Exception e) {
                    LogUtil.getLogger().debug("获取微服务[{}]提供的Api initTime失败：{}", serverName, e.getMessage());
                }
            }

            if (index != -1) {
                Map<String, Api> apisOnServer;
                ServerApi serverApi;
                LogUtil.getLogger().debug("正在更新微服务[{}]的api, url: {}", serverName, instances.get(index).getUri());
                String getApisUrl = instances.get(index).getUri() + "/api";
                try {
                    Result result = noBalanceRestTemplate.getForObject(getApisUrl, Result.class);
                    apisOnServer = null;
                    microServerApis.put(serverName.toUpperCase(), serverApi);
                    updateCount++;
                    LogUtil.getLogger().debug("更新微服务[{}]api成功：{}", serverName, serverApi);
                } catch (Exception e) {
                    LogUtil.getLogger().info("获取微服务[{}]提供的Tcp api失败：{}", serverName, e.getMessage());
                }
            }
        }
        LogUtil.getLogger().debug("已更新[{}]个微服务的api", updateCount);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        updateEurekaTcpServer();
    }
}
