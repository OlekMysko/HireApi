package atipera.com.hireapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "github")
@Getter
@Setter
public class URLConfiguration {
    private String api_url;
    private String repositories_url_suffix;
    private String user_url_suffix;
    private String branches_url_suffix_template;
    private String token;
}