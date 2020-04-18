package com.imooc;

import com.imooc.netty.WSServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {

    // 监听springboot启动netty服务器
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext().getParent() == null){
            try {
                WSServer.getInstance().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
