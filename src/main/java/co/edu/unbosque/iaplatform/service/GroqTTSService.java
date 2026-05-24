package co.edu.unbosque.iaplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Servicio para conversión de texto a voz usando Groq Orpheus TTS.
 * Soporta inglés y árabe (modelo específico).
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class GroqTTSService {

    @Value("${api.groq.key:}")
    private String groqKey;

    private final WebClient webClient;

    public GroqTTSService(WebClient.Builder builder) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();
        this.webClient = builder
            .baseUrl("https://api.groq.com/openai/v1")
            .exchangeStrategies(strategies)
            .build();
    }

    /**
     * Convierte texto en inglés a audio WAV.
     *
     * @param texto Texto a convertir (máximo 500 caracteres)
     * @return Array de bytes del audio WAV
     */
    public Mono<byte[]> textToSpeech(String texto) {
        if (groqKey == null || groqKey.isBlank()) {
            return Mono.error(new RuntimeException("Configura api.groq.key para usar TTS."));
        }

        String textoLimitado = texto.length() > 500 ? texto.substring(0, 500) : texto;

        Map<String, Object> body = Map.of(
            "model", "canopylabs/orpheus-v1-english",
            "input", textoLimitado,
            "voice", "daniel",
            "response_format", "wav"
        );

        return webClient.post()
                .uri("/audio/speech")
                .header("Authorization", "Bearer " + groqKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), resp ->
                    resp.bodyToMono(String.class).flatMap(errorBody ->
                        Mono.error(new RuntimeException("Groq TTS error " + resp.statusCode() + ": " + errorBody))
                    )
                )
                .bodyToMono(byte[].class);
    }

    /**
     * Convierte texto en árabe a audio WAV.
     *
     * @param texto Texto en árabe a convertir
     * @return Array de bytes del audio WAV
     */
    public Mono<byte[]> textToSpeechArabic(String texto) {
        if (groqKey == null || groqKey.isBlank()) {
            return Mono.error(new RuntimeException("Configura api.groq.key para usar TTS."));
        }

        String textoLimitado = texto.length() > 500 ? texto.substring(0, 500) : texto;

        Map<String, Object> body = Map.of(
            "model", "canopylabs/orpheus-arabic-saudi",
            "input", textoLimitado,
            "voice", "default",
            "response_format", "wav"
        );

        return webClient.post()
                .uri("/audio/speech")
                .header("Authorization", "Bearer " + groqKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), resp ->
                    resp.bodyToMono(String.class).flatMap(errorBody ->
                        Mono.error(new RuntimeException("Groq TTS Arabic HTTP " + resp.statusCode() + ": " + errorBody))
                    )
                )
                .bodyToMono(byte[].class);
    }
}
