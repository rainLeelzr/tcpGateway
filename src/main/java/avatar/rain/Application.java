package avatar.rain;

import avatar.rain.core.api.Api;
import avatar.rain.core.api.ApiManager;
import avatar.rain.core.net.atcp.request.worker.RequestHandleWorkerPool;
import avatar.rain.core.net.atcp.server.TcpServer;
import avatar.rain.core.serialization.ProtobufSerializationManager;
import avatar.rain.core.util.bean.BeanUtil;
import avatar.rain.core.util.log.LogUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
  SpringCLoud中的“Discovery Service”有多种实现，比如：eureka, consul, zookeeper。
  1，@EnableDiscoveryClient注解是基于spring-cloud-commons依赖，并且在classpath中实现；
  2，@EnableEurekaClient注解是基于spring-cloud-netflix依赖，只能为eureka作用；
 */
@SpringBootApplication
@EnableEurekaClient
public class Application {

    //启动服务时，开启debug日志模式：java -jar xxx.jar --debug
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Application.class)
                .web(true)
                .run(args);

        initApiManager(ctx);

        initRequestHandleWorkerPool(ctx);

        initProtobufSerializationManager(ctx);

        initTcpServer(ctx);
    }

    private static void initTcpServer(ApplicationContext ctx) {
        TcpServer tcpServer = BeanUtil.getBeanByNameOfBeanType(ctx, TcpServer.class);
        tcpServer.start();
    }

    private static void initApiManager(ApplicationContext ctx) {
        ApiManager apiManager = BeanUtil.getBeanByNameOfBeanType(ctx, ApiManager.class);
        apiManager.init(ctx);
    }

    private static void initRequestHandleWorkerPool(ApplicationContext ctx) {
        RequestHandleWorkerPool requestHandleWorkerPool = BeanUtil.getBeanByNameOfBeanType(ctx, RequestHandleWorkerPool.class);
        requestHandleWorkerPool.initWorkers();
    }

    /**
     * 初始化protobuf的序列化管理器
     */
    private static void initProtobufSerializationManager(ApplicationContext ctx) {
        ProtobufSerializationManager serializationManager = BeanUtil.getBeanByNameOfBeanType(ctx, ProtobufSerializationManager.class);

        ApiManager apiManager = BeanUtil.getBeanByNameOfBeanType(ctx, ApiManager.class);
        Collection<Api> values = apiManager.getApis().values();
        Map<String, Method> methods = new HashMap<>();

        values.forEach(a -> {
            if (!methods.containsKey(a.getProtobuf())) {
                try {
                    Class<?> protoClass = Class.forName(a.getProtobuf());
                    Method parseFrom = protoClass.getMethod("parseFrom", byte[].class);
                    methods.put(a.getProtobuf(), parseFrom);
                } catch (ClassNotFoundException e) {
                    LogUtil.getLogger().error("找不到api定义的protobuf类：{}", a.toString());
                    System.exit(0);
                } catch (NoSuchMethodException e) {
                    LogUtil.getLogger().error("api定义的protobuf类中找不到parseFrom方法：{}", a.toString());
                    System.exit(0);
                }
            }

        });

        serializationManager.init(methods);
    }

}
