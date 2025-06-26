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

// This class handles incoming HTTP requests and validates JWT tokens before forwarding the requests to downstream services.
// If we want to build a custom filter we need to extend AbstractGatewayFilterFactory, which is how filters are built in Spring Cloud Gateway.

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    Environment env;

    @Autowired
    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

//  This class is a placeholder for any configuration options you might want to pass to your filter. It's empty here, but it's required by AbstractGatewayFilterFactory.
    public static class Config {
        // define configuration properties here
    }

    @Override
    public GatewayFilter apply(Config config) {
       /*
        ServerWebExchange exchange : It represents the entire HTTP request-response exchange.
        we can :-
            Get the request object : exchange.getRequest();
            Get the response object: exchange.getResponse();
            Read or modify headers, path, query parameters, etc.
            Set status codes or send error responses.

        GatewayFilterChain chain : A pipeline of filters, and chain.filter(exchange) just means "Pass this request to the next filter in the chain".
        If our filter returns early (like in error cases), the rest of the chain is not executed.
        */

        return ((exchange, chain) -> {
            ServerHttpRequest serverHttpRequest = exchange.getRequest();
            if (!serverHttpRequest.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
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
            SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes); // The secret key from application.properties is encoded with Base64, then used to build a SecretKey.
            JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build(); // Parses and verifies the JWT using the secretKey.
            subject = jwtParser.parseSignedClaims(jwt).getPayload().getSubject(); // Extracts the subject from the JWT payload, which usually identifies the user.
        }catch (Exception e){
            returnValue = false;
        }
        if(subject == null || subject.isEmpty()){
            returnValue = false;
        }
        return returnValue;
    }
}
