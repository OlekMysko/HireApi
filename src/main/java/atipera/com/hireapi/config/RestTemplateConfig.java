package atipera.com.hireapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RestTemplateConfig {

    @Bean
    public WebClient.Builder webClientBuilder(URLConfiguration urlConfig) {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "token " + urlConfig.getToken());
    }
}