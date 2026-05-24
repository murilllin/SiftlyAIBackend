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
 * Servicio para integración con NVIDIA NIM (Modelos Llama 3.3 y Nemotron).
 * Proporciona generación de respuestas con soporte de historial conversacional.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class NvidiaLLMService {

    @Value("${api.nvidia.nim.key}")
    private String apiKey;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private WebClient webClient;

    /**
     * Genera respuesta sin historial (compatibilidad).
     *
     * @param prompt  Prompt del usuario
     * @param modelId ID del modelo NVIDIA a usar
     * @return Respuesta generada por NVIDIA
     */
    public Mono<String> generarRespuesta(String prompt, String modelId) {
        return generarRespuesta(List.of(), prompt, modelId);
    }

    /**
     * Genera respuesta con historial conversacional.
     *
     * @param historial Lista de mensajes previos con role y content
     * @param prompt    Prompt actual del usuario
     * @param modelId   ID del modelo NVIDIA a usar
     * @return Respuesta generada por NVIDIA
     */
    public Mono<String> generarRespuesta(List<Map<String, String>> historial, String prompt, String modelId) {
        if (webClient == null) {
            webClient = webClientBuilder.baseUrl("https://integrate.api.nvidia.com/v1").build();
        }

        List<Map<String, String>> messages = new ArrayList<>(historial);
        messages.add(Map.of("role", "user", "content", prompt));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelId);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        var choices = (List<Map<String, Object>>) response.get("choices");
                        var message = (Map<String, Object>) choices.get(0).get("message");
                        return (String) message.get("content");
                    } catch (Exception e) {
                        return "Error procesando: " + e.getMessage();
                    }
                })
                .onErrorResume(e -> Mono.just("Error: " + e.getMessage()));
    }
}