package co.edu.unbosque.iaplatform.controller;

import co.edu.unbosque.iaplatform.entity.Mensaje;
import co.edu.unbosque.iaplatform.entity.RespuestaIA;
import co.edu.unbosque.iaplatform.entity.RespuestaIA.ModeloIA;
import co.edu.unbosque.iaplatform.repository.MensajeRepository;
import co.edu.unbosque.iaplatform.repository.RespuestaIARepository;
import co.edu.unbosque.iaplatform.service.GroqTTSService;
import co.edu.unbosque.iaplatform.service.OpenRouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import reactor.core.publisher.Mono;

/**
 * Controlador para generación de contenido multimedia.
 * <p>
 * Proporciona endpoints para:
 * <ul>
 *   <li>Texto a voz (TTS) con Groq Orpheus</li>
 *   <li>Generación de imágenes con xAI Grok y Recraft</li>
 *   <li>Generación de videos con Wan 2.6, Veo 3.1 Lite y xAI Grok</li>
 * </ul>
 * </p>
 * 
 * @author Daniel Murillo
 * @version 1.0
 */
@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired private GroqTTSService groqTTSService;
    @Autowired private OpenRouterService openRouterService;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private RespuestaIARepository respuestaIARepository;

    /**
     * Convierte texto a audio usando Groq Orpheus TTS.
     * Si se proporciona mensajeId, persiste la respuesta en auditoría.
     *
     * @param req Mapa con "texto" y opcional "mensajeId"
     * @return Archivo de audio WAV
     */
    @PostMapping("/tts")
    public ResponseEntity<?> tts(@RequestBody Map<String, Object> req) {
        String texto = req.getOrDefault("texto", "").toString();
        if (texto.isBlank()) {
            return ResponseEntity.badRequest().body(new byte[0]);
        }

        Object mensajeIdObj = req.get("mensajeId");

        try {
            long t0 = System.currentTimeMillis();
            byte[] audioBytes = groqTTSService.textToSpeech(texto).block();
            if (audioBytes == null || audioBytes.length == 0) {
                return ResponseEntity.internalServerError().body(new byte[0]);
            }
            long tiempoMs = System.currentTimeMillis() - t0;

            if (mensajeIdObj != null) {
                try {
                    long mensajeId = Long.parseLong(mensajeIdObj.toString());
                    Optional<Mensaje> mensajeOpt = mensajeRepository.findById(mensajeId);
                    if (mensajeOpt.isPresent()) {
                        String audioBase64 = "data:audio/wav;base64," + Base64.getEncoder().encodeToString(audioBytes);
                        RespuestaIA respuesta = new RespuestaIA();
                        respuesta.setMensaje(mensajeOpt.get());
                        respuesta.setModeloIA(ModeloIA.GROQ_TTS_ORPHEUS);
                        respuesta.setRespuesta("");
                        respuesta.setTiempoRespuestaMs(tiempoMs);
                        respuesta.setUrlArchivo(audioBase64);
                        respuestaIARepository.save(respuesta);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ TTS: no se pudo persistir RespuestaIA: " + e.getMessage());
                }
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"audio.wav\"")
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .body(audioBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new byte[0]);
        }
    }

    /**
     * Convierte texto a audio en árabe usando Groq Orpheus Arabic.
     *
     * @param req Mapa con "texto"
     * @return Archivo de audio WAV
     */
    @PostMapping("/tts-arabic")
    public ResponseEntity<byte[]> ttsArabic(@RequestBody Map<String, String> req) {
        String texto = req.getOrDefault("texto", "");
        if (texto.isBlank()) {
            return ResponseEntity.badRequest().body(new byte[0]);
        }
        try {
            byte[] audioBytes = groqTTSService.textToSpeechArabic(texto).block();
            if (audioBytes == null || audioBytes.length == 0) {
                return ResponseEntity.internalServerError().body(new byte[0]);
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"audio-arabic.wav\"")
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .body(audioBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new byte[0]);
        }
    }

    /**
     * Genera video usando Wan 2.6.
     *
     * @param req Mapa con "prompt"
     * @return URL del video generado
     */
    @PostMapping("/video/wan")
    public Mono<ResponseEntity<String>> videoWan(@RequestBody Map<String, String> req) {
        String prompt = req.getOrDefault("prompt", "");
        if (prompt.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body("Prompt requerido"));
        }
        return openRouterService.generarVideoWan(prompt)
                .map(url -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(url))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage())));
    }

    /**
     * Genera video usando Veo 3.1 Lite.
     *
     * @param req Mapa con "prompt"
     * @return URL del video generado
     */
    @PostMapping("/video/veo-lite")
    public Mono<ResponseEntity<String>> videoVeoLite(@RequestBody Map<String, String> req) {
        String prompt = req.getOrDefault("prompt", "");
        if (prompt.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body("Prompt requerido"));
        }
        return openRouterService.generarVideoVeoLite(prompt)
                .map(url -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(url))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage())));
    }

    /**
     * Genera video usando xAI Grok.
     *
     * @param req Mapa con "prompt"
     * @return URL del video generado
     */
    @PostMapping("/video/xai")
    public Mono<ResponseEntity<String>> videoXAI(@RequestBody Map<String, String> req) {
        String prompt = req.getOrDefault("prompt", "");
        if (prompt.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body("Prompt requerido"));
        }
        return openRouterService.generarVideoXAI(prompt)
                .map(url -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(url))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage())));
    }

    /**
     * Genera imagen usando xAI Grok.
     *
     * @param req Mapa con "prompt"
     * @return URL de la imagen generada
     */
    @PostMapping("/imagen/xai")
    public Mono<ResponseEntity<String>> imagenXAI(@RequestBody Map<String, String> req) {
        String prompt = req.getOrDefault("prompt", "");
        if (prompt.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body("Prompt requerido"));
        }
        return openRouterService.generarImagenXAI(prompt)
                .map(url -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(url))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage())));
    }

    /**
     * Genera imagen usando Recraft v4.1.
     *
     * @param req Mapa con "prompt"
     * @return URL de la imagen generada
     */
    @PostMapping("/imagen/recraft")
    public Mono<ResponseEntity<String>> imagenRecraft(@RequestBody Map<String, String> req) {
        String prompt = req.getOrDefault("prompt", "");
        if (prompt.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body("Prompt requerido"));
        }
        return openRouterService.generarImagenRecraft(prompt)
                .map(url -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(url))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage())));
    }
}
