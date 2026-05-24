package co.edu.unbosque.iaplatform.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

/**
 * Servicio para integración con OpenRouter.
 * Genera imágenes y videos usando múltiples modelos (xAI Grok, Recraft, Wan, Veo).
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class OpenRouterService {

    @Value("${api.openrouter.key}")
    private String apiKey;

    private static final String BASE_URL = "https://openrouter.ai/api/v1";
    private static final Duration TIMEOUT = Duration.ofSeconds(45);

    @Autowired
    private WebClient.Builder webClientBuilder;

    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            synchronized (this) {
                if (webClient == null) {
                    HttpClient httpClient = HttpClient.create()
                            .responseTimeout(TIMEOUT)
                            .followRedirect(true);
                    webClient = webClientBuilder
                            .baseUrl(BASE_URL)
                            .clientConnector(new ReactorClientHttpConnector(httpClient))
                            .build();
                }
            }
        }
        return webClient;
    }

    /**
     * Genera imagen usando xAI Grok.
     *
     * @param prompt Descripción de la imagen
     * @return URL de la imagen generada
     */
    public Mono<String> generarImagenXAI(String prompt) {
        return generarImagenChatCompletions("x-ai/grok-imagine-image-quality", prompt, false);
    }

    /**
     * Genera imagen usando Recraft v4.1.
     *
     * @param prompt Descripción de la imagen
     * @return URL de la imagen generada
     */
    public Mono<String> generarImagenRecraft(String prompt) {
        return generarImagenChatCompletions("recraft/recraft-v4.1-utility", prompt, false);
    }

    private Mono<String> generarImagenChatCompletions(String model, String prompt, boolean includeText) {
        List<String> modalities = includeText ? List.of("image", "text") : List.of("image");

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "modalities", modalities);

        return getWebClient().post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "https://siftlyai.unbosque.edu.co")
                .header("X-Title", "SiftlyAI")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        resp -> resp.bodyToMono(String.class).flatMap(err ->
                            Mono.error(new RuntimeException("IMG_ERR: " + err))))
                .bodyToMono(Map.class)
                .timeout(TIMEOUT)
                .map(response -> extraerImagenDeResponse(response, model))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume((Function<Throwable, Mono<String>>) error ->
                    Mono.just("Error OpenRouter imagen " + model + ": " + error.getMessage()));
    }

    @SuppressWarnings("unchecked")
    private String extraerImagenDeResponse(Map<String, Object> response, String model) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) return "Error: sin choices";

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) return "Error: sin message";

            Object imagesRaw = message.get("images");
            if (imagesRaw instanceof List) {
                List<Map<String, Object>> images = (List<Map<String, Object>>) imagesRaw;
                for (Map<String, Object> img : images) {
                    if ("image_url".equals(img.get("type"))) {
                        Map<String, String> imgUrl = (Map<String, String>) img.get("image_url");
                        if (imgUrl != null && imgUrl.get("url") != null) {
                            return imgUrl.get("url");
                        }
                    }
                }
            }

            Object content = message.get("content");
            if (content instanceof String) return (String) content;

            if (content instanceof List) {
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content;
                for (Map<String, Object> part : parts) {
                    if (part.containsKey("image_url")) {
                        Map<String, String> imgUrl = (Map<String, String>) part.get("image_url");
                        return imgUrl.get("url");
                    }
                    if (part.containsKey("text")) return (String) part.get("text");
                }
            }
            return "Error: formato de respuesta inesperado para " + model;
        } catch (Exception e) {
            return "Error procesando imagen " + model + ": " + e.getMessage();
        }
    }

    /**
     * Genera video usando Wan 2.6.
     *
     * @param prompt Descripción del video
     * @return URL del video generado
     */
    public Mono<String> generarVideoWan(String prompt) {
        return generarVideo("alibaba/wan-2.6", prompt);
    }

    /**
     * Genera video usando Veo 3.1 Lite.
     *
     * @param prompt Descripción del video
     * @return URL del video generado
     */
    public Mono<String> generarVideoVeoLite(String prompt) {
        return generarVideo("google/veo-3.1-lite", prompt);
    }

    /**
     * Genera video usando xAI Grok.
     *
     * @param prompt Descripción del video
     * @return URL del video generado
     */
    public Mono<String> generarVideoXAI(String prompt) {
        return generarVideo("x-ai/grok-imagine-video", prompt);
    }

    private Mono<String> generarVideo(String model, String prompt) {
        int duration;
        String modelName;

        if (model.contains("wan")) {
            duration = 5;
            modelName = "Wan 2.6";
        } else if (model.contains("veo")) {
            duration = 4;
            modelName = "Veo 3.1 Lite";
        } else if (model.contains("grok")) {
            duration = 3;
            modelName = "xAI Grok Video";
        } else {
            duration = 5;
            modelName = model;
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "duration", duration
        );

        return getWebClient().post()
                .uri("/videos")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "https://siftlyai.unbosque.edu.co")
                .header("X-Title", "SiftlyAI")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        resp -> resp.bodyToMono(String.class).flatMap(err ->
                            Mono.error(new RuntimeException("VID_ERR: " + err))))
                .bodyToMono(Map.class)
                .timeout(TIMEOUT)
                .flatMap(response -> {
                    String pollingUrl = (String) response.get("polling_url");
                    if (pollingUrl == null) {
                        return extraerVideoDirecto(response, modelName);
                    }
                    return pollVideo(pollingUrl, modelName);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume((Function<Throwable, Mono<String>>) error ->
                    Mono.just("Error OpenRouter video: " + error.getMessage()));
    }

    private Mono<String> pollVideo(String pollingUrl, String modelName) {
        return getWebClient().get()
                .uri(pollingUrl)
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("POLL_ERR: " + err))))
                .bodyToMono(Map.class)
                .timeout(TIMEOUT)
                .flatMap(res -> {
                    String status = (String) res.get("status");
                    if ("completed".equals(status) || "success".equals(status)) {
                        return extraerVideoDirecto(res, modelName);
                    } else if ("failed".equals(status) || "error".equals(status)) {
                        String errorMsg = res.containsKey("error") ? res.get("error").toString() : "Generación fallida";
                        return Mono.just("Error video " + modelName + ": " + errorMsg);
                    }
                    return Mono.error(new RuntimeException("PENDING"));
                })
                .retryWhen(Retry.backoff(30, Duration.ofSeconds(5))
                        .filter(throwable -> throwable instanceof RuntimeException
                                && "PENDING".equals(throwable.getMessage())))
                .onErrorResume((Function<Throwable, Mono<String>>) error -> {
                    String msg = error.getMessage();
                    if (msg != null && msg.contains("PENDING")) {
                        return Mono.just("Error " + modelName + ": tiempo de espera agotado");
                    }
                    return Mono.just("Error polling " + modelName + ": " + msg);
                });
    }

    @SuppressWarnings("unchecked")
    private Mono<String> extraerVideoDirecto(Map<String, Object> response, String modelName) {
        try {
            if (response.containsKey("unsigned_urls")) {
                Object urlsObj = response.get("unsigned_urls");
                if (urlsObj instanceof List) {
                    List<String> urls = (List<String>) urlsObj;
                    if (!urls.isEmpty()) {
                        return Mono.just(urls.get(0));
                    }
                }
            }
            if (response.containsKey("url")) {
                return Mono.just((String) response.get("url"));
            }
            return Mono.just("Error: formato inesperado para " + modelName);
        } catch (Exception e) {
            return Mono.just("Error extrayendo video: " + e.getMessage());
        }
    }
}