package co.edu.unbosque.iaplatform.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Servicio para generación de imágenes usando Pollinations.ai.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class ImagenService {

    /**
     * Genera una URL de imagen usando Pollinations.ai.
     *
     * @param prompt Descripción de la imagen a generar
     * @return URL de la imagen generada
     */
    public Mono<String> generarImagenPollinations(String prompt) {
        String imageUrl = "https://image.pollinations.ai/prompt/" +
                         URLEncoder.encode(prompt, StandardCharsets.UTF_8);
        return Mono.just(imageUrl);
    }
}