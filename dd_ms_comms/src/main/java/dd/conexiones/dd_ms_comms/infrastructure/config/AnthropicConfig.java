package dd.conexiones.dd_ms_comms.infrastructure.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicConfig {

    @Bean
    public AnthropicClient anthropicClient(@Value("${anthropic.api-key:}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            // Permite arrancar sin credencial configurada; fallara solo al invocar la API.
            return AnthropicOkHttpClient.fromEnv();
        }
        return AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }
}
