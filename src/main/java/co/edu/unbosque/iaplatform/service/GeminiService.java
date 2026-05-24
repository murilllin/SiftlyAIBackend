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
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para integración con Gemini 2.5 Flash.
 * Proporciona generación de respuestas con soporte de historial conversacional.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class GeminiService {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    @Value("${api.gemini.key}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiService() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
            .responseTimeout(Duration.ofSeconds(90))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(90, TimeUnit.SECONDS))
                .addHandlerLast(new WriteTimeoutHandler(90, TimeUnit.SECONDS)));
        this.webClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .build();
    }

    /**
     * Genera respuesta sin historial (compatibilidad).
     *
     * @param prompt Prompt del usuario
     * @return Respuesta generada por Gemini
     */
    public Mono<String> generarRespuesta(String prompt) {
        return generarRespuesta(List.of(), prompt);
    }

    /**
     * Genera respuesta con historial conversacional.
     *
     * @param historial Lista de mensajes previos con role y content
     * @param prompt    Prompt actual del usuario
     * @return Respuesta generada por Gemini
     */
    public Mono<String> generarRespuesta(List<Map<String, String>> historial, String prompt) {
        List<Map<String, Object>> contents = new ArrayList<>();
        for (Map<String, String> h : historial) {
            String role = "assistant".equals(h.get("role")) ? "model" : h.get("role");
            contents.add(Map.of(
                "role", role,
                "parts", List.of(Map.of("text", h.get("content")))
            ));
        }
        contents.add(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", prompt))
        ));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("system_instruction", Map.of(
            "parts", List.of(Map.of("text",
                "Eres un asistente dispuesto a responder al usuario de la mejor manera posible. " +
                "Tus respuestas deben ser detalladas, técnicas y usar un tono cercano y motivador."))
        ));
        requestBody.put("contents", contents);
        requestBody.put("generationConfig", Map.of("temperature", 0.7, "maxOutputTokens", 4096));

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/models/gemini-2.5-flash:generateContent")
                    .queryParam("key", apiKey)
                    .build())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                    .filter(t -> t.getMessage() != null && t.getMessage().contains("429")))
                .map(response -> {
                    try {
                        var candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates == null || candidates.isEmpty()) {
                            return "Gemini: respuesta bloqueada por políticas de seguridad.";
                        }
                        var content = (Map<String, Object>) candidates.get(0).get("content");
                        var parts = (List<Map<String, Object>>) content.get("parts");
                        for (Map<String, Object> part : parts) {
                            if (part.containsKey("text") && !Boolean.TRUE.equals(part.get("thought"))) {
                                return (String) part.get("text");
                            }
                        }
                        return (String) parts.get(parts.size() - 1).get("text");
                    } catch (Exception e) {
                        return "Error procesando respuesta de Gemini: " + e.getMessage();
                    }
                })
                .onErrorResume(e -> Mono.just("Error de Gemini: " + e.getMessage()));
    }
}