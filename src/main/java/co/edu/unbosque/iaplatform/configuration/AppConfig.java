package co.edu.unbosque.iaplatform.configuration;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración general de la aplicación.
 * Define beans compartidos como RestTemplate con timeouts optimizados.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(10))
            .setResponseTimeout(Timeout.ofSeconds(60))
            .build();

        CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();

        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}