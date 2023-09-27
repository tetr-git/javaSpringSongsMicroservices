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
                .route("playlists_route", r -> r
                        .path("/playlists/**")
                        .uri("http://localhost:8083")
                )
                // Add more routes as needed
                .build();
    }
}
