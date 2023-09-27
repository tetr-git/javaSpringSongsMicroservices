package songms.api_gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Define routes directly
                .route("auth_route", r -> r
                        .path("/auth/**")
                        .uri("http://localhost:8081")
                )
                .route("songs_route", r -> r
                        .path("/songs/**")
                        .uri("http://localhost:8082")
                )
                .route("song_lists", r -> r
                        .path("/song_lists/**")
                        .uri("http://localhost:8083")
                )
                .route("spotify", r -> r
                        .path("/spotify/**")
                        .uri("http://localhost:8084")
                )
                .build();
    }
}
