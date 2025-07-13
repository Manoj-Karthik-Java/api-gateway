package com.appsdeveloperblog.photoapp.api.ApiGateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MyPostFilter implements GlobalFilter, Ordered {

    //    We implemented the Ordered interface to give the order in which our MyPostFilter is executed
    //    In this case this MyPostFilter is executed last

    final Logger logger = LoggerFactory.getLogger(MyPostFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            logger.info("My first post-filter is executed");
        }));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
