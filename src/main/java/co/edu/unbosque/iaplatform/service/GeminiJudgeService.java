package co.edu.unbosque.iaplatform.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Servicio juez que utiliza Gemini para evaluar y seleccionar la mejor respuesta
 * entre múltiples modelos de IA. Tiene su propio WebClient para no afectar
 * el rate limit del servicio principal de Gemini.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class GeminiJudgeService {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    @Value("${api.gemini.judge.key:${api.gemini.key}}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiJudgeService() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
            .build();
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
            .responseTimeout(Duration.ofSeconds(60))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS)));
        this.webClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .build();
    }

    /**
     * Evalúa las respuestas de los modelos y devuelve el índice de la mejor.
     *
     * @param promptUsuario Prompt original del usuario
     * @param modelos       Lista de nombres de modelo (mismo orden que respuestas)
     * @param respuestas    Lista de textos de respuesta
     * @return Índice de la mejor respuesta (0-based) o -1 en caso de error
     */
    public Mono<Integer> elegirMejor(String promptUsuario, List<String> modelos, List<String> respuestas) {
        if (respuestas == null || respuestas.isEmpty()) return Mono.just(-1);
        if (respuestas.size() == 1) return Mono.just(0);

        StringBuilder sb = new StringBuilder();
        sb.append("Eres un evaluador experto e imparcial de respuestas de inteligencia artificial.\n\n");
        sb.append("PREGUNTA DEL USUARIO:\n").append(promptUsuario).append("\n\n");
        sb.append("RESPUESTAS A EVALUAR:\n");
        for (int i = 0; i < respuestas.size(); i++) {
            sb.append("RESPUESTA ").append(i).append(" (").append(modelos.get(i)).append(")\n");
            String r = respuestas.get(i);
            sb.append(r.length() > 800 ? r.substring(0, 800) + "..." : r).append("\n\n");
        }
        sb.append("INSTRUCCIÓN: Analiza cuál respuesta es más completa, precisa, clara y útil para el usuario. ");
        sb.append("Responde ÚNICAMENTE con el número entero del índice de la mejor respuesta (0, 1, 2, etc.). ");
        sb.append("No expliques nada, solo el número.");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
            Map.of("role", "user", "parts", List.of(Map.of("text", sb.toString())))
        ));
        requestBody.put("generationConfig", Map.of("temperature", 0.0, "maxOutputTokens", 5));

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/models/gemini-2.5-flash:generateContent")
                    .queryParam("key", apiKey)
                    .build())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        var candidates = (List<Map<String, Object>>) response.get("candidates");
                        var content = (Map<String, Object>) candidates.get(0).get("content");
                        var parts = (List<Map<String, Object>>) content.get("parts");
                        String raw = ((String) parts.get(0).get("text")).trim();
                        int idx = Integer.parseInt(raw.replaceAll("[^0-9]", ""));
                        if (idx >= 0 && idx < respuestas.size()) {
                            return idx;
                        }
                        return 0;
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .onErrorResume(e -> Mono.just(-1));
    }
}
