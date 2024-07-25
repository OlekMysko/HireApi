package atipera.com.hireapi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class URLConfiguration {
    @Value("${github.api.url}")
    private String githubApiUrl;

    @Value("${repositories.url.suffix}")
    private String repositoriesUrlSuffix;

    @Value("${user.url.suffix}")
    private String userUrlSuffix;

    @Value("${branches.url.suffix.template}")
    private String branchesUrlSuffixTemplate;

}