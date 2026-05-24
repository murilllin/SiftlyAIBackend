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
 * Servicio para integración con Groq (modelo Llama 3.3 70B).
 * Proporciona generación de respuestas con soporte de historial conversacional.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class GroqService {

    @Value("${api.groq.key}")
    private String apiKey;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private WebClient webClient;

    /**
     * Genera respuesta sin historial (compatibilidad).
     *
     * @param prompt Prompt del usuario
     * @return Respuesta generada por Groq
     */
    public Mono<String> generarRespuesta(String prompt) {
        return generarRespuesta(List.of(), prompt);
    }

    /**
     * Genera respuesta con historial conversacional.
     *
     * @param historial Lista de mensajes previos con role y content
     * @param prompt    Prompt actual del usuario
     * @return Respuesta generada por Groq
     */
    public Mono<String> generarRespuesta(List<Map<String, String>> historial, String prompt) {
        if (webClient == null) {
            webClient = webClientBuilder.baseUrl("https://api.groq.com/openai/v1").build();
        }

        List<Map<String, String>> messages = new ArrayList<>(historial);
        messages.add(Map.of("role", "user", "content", prompt));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 1024);

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        Map<String, Object> firstChoice = ((List<Map<String, Object>>) response.get("choices")).get(0);
                        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                        return (String) message.get("content");
                    } catch (Exception e) {
                        return "Error procesando respuesta";
                    }
                })
                .onErrorResume(e -> Mono.just("Error: " + e.getMessage()));
    }
}