package co.edu.unbosque.iaplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para integración con la API de Cohere (modelo Command-R Plus).
 * Proporciona generación de respuestas con soporte de historial conversacional.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class CohereService {

    @Value("${api.cohere.key}")
    private String apiKey;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private WebClient webClient;

    /**
     * Genera respuesta sin historial (compatibilidad).
     *
     * @param prompt Prompt del usuario
     * @return Respuesta generada por Cohere
     */
    public Mono<String> generarRespuesta(String prompt) {
        return generarRespuesta(List.of(), prompt);
    }

    /**
     * Genera respuesta con historial conversacional.
     *
     * @param historial Lista de mensajes previos con role y content
     * @param prompt    Prompt actual del usuario
     * @return Respuesta generada por Cohere
     */
    public Mono<String> generarRespuesta(List<Map<String, String>> historial, String prompt) {
        if (webClient == null) {
            webClient = webClientBuilder.baseUrl("https://api.cohere.ai/v2").build();
        }

        List<Map<String, String>> messages = new ArrayList<>(historial);
        messages.add(Map.of("role", "user", "content", prompt));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "command-r-plus-08-2024");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        return webClient.post()
                .uri("/chat")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        if (response.containsKey("message")) {
                            Map<String, Object> message = (Map<String, Object>) response.get("message");
                            if (message.containsKey("content")) {
                                List<Map<String, Object>> contentList = (List<Map<String, Object>>) message.get("content");
                                if (contentList != null && !contentList.isEmpty()) {
                                    return (String) contentList.get(0).get("text");
                                }
                            }
                        }
                        return "Respuesta generada por Cohere";
                    } catch (Exception e) {
                        return "Error procesando: " + e.getMessage();
                    }
                })
                .onErrorResume(e -> {
                    return Mono.just("Error de Cohere: " + e.getMessage());
                });
    }
}