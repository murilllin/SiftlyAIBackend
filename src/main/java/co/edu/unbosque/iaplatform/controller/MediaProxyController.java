package co.edu.unbosque.iaplatform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * Controlador proxy para contenido multimedia de APIs externas.
 * <p>
 * Este controlador actúa como intermediario para evitar problemas de CORS
 * cuando el frontend necesita acceder a imágenes o videos generados por
 * APIs externas (OpenRouter, Pollinations, etc.).
 * </p>
 * 
 * @author Daniel Murillo
 * @version 1.0
 */
@RestController
@RequestMapping("/api/proxy")
public class MediaProxyController {

    @Value("${api.openrouter.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Proxy para videos de APIs externas.
     *
     * @param url URL del video original
     * @return Video en formato MP4 o WEBM
     */
    @GetMapping("/video")
    public ResponseEntity<byte[]> proxyVideo(@RequestParam String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
            );
            
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (url.contains(".mp4")) {
                mediaType = MediaType.parseMediaType("video/mp4");
            } else if (url.contains(".webm")) {
                mediaType = MediaType.parseMediaType("video/webm");
            }
            
            return ResponseEntity.ok()
                .contentType(mediaType)
                .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Proxy para imágenes de APIs externas.
     *
     * @param url URL de la imagen original
     * @return Imagen en formato PNG
     */
    @GetMapping("/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
            );
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}