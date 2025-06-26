package security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    Environment env;

    @Autowired
    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    public static class Config {
        // define configuration properties here
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest serverHttpRequest = exchange.getRequest();
            if (serverHttpRequest.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
                return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);

            String authorizationHeader = serverHttpRequest.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer", "");

            if(!isJwtValid(jwt))
                return onError(exchange,"JWT token is not valid",HttpStatus.UNAUTHORIZED);

            return chain.filter(exchange);
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String noAuthorizationHeader, HttpStatus httpStatus) {
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        serverHttpResponse.setStatusCode(httpStatus);

        return serverHttpResponse.setComplete();
    }

    private boolean isJwtValid(String jwt){
        boolean returnValue = true;
        String subject = null;
        try{
            byte[] secretKeyBytes = Base64.getEncoder().encode(env.getProperty("token.secret").getBytes());
            SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
            JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
            subject = jwtParser.parseSignedClaims(jwt).getPayload().getSubject();
        }catch (Exception e){
            returnValue = false;
        }
        if(subject == null || subject.isEmpty()){
            returnValue = false;
        }
        return returnValue;
    }
}
