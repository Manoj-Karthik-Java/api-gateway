package security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

// For simple header manipulation, use route-specific filters.
// Global filters are useful when the logic is shared across all routes and not specific to any single service or endpoint.
// In Spring Cloud Gateway, a prefilter refers to logic that is executed before the request is routed to the target backend service
// To create a global prefilter and postfilter, you need to implement the GlobalFilter interface and optionally use Ordered to set execution priority.


// Spring can distinguish between postfilter and prefilter with the return value of filter() method
// In postfilter method the return value looks like this
//   return chain.filter(exchange).then(Mono.fromRunnable(()->{
//            logger.info("My first pre-filter is executed");
//        }));
@Component
public class MyPreFilter implements GlobalFilter {

    final Logger logger = LoggerFactory.getLogger(MyPreFilter.class);
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("My first pre-filter is executed");

        String requestPath = exchange.getRequest().getPath().toString();
        logger.info("Request path " + requestPath);

        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();

        Set<String> headerNames = httpHeaders.keySet();
        for (String headerName : headerNames) {
            String headerValue = httpHeaders.getFirst(headerName);
            logger.info(headerName + " " + headerValue);
        }

        return chain.filter(exchange);
    }
}
