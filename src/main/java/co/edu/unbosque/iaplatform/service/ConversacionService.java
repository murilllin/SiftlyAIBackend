package co.edu.unbosque.iaplatform.service;

import co.edu.unbosque.iaplatform.entity.*;
import co.edu.unbosque.iaplatform.entity.RespuestaIA.ModeloIA;
import co.edu.unbosque.iaplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio principal para gestión de conversaciones y mensajes.
 * Orquesta la generación de respuestas de múltiples modelos de IA,
 * maneja el historial conversacional y selecciona la mejor respuesta.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class ConversacionService {

    @Autowired private ConversacionRepository conversacionRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private RespuestaIARepository respuestaIARepository;
    @Autowired private GroqService groqService;
    @Autowired private CohereService cohereService;
    @Autowired private MistralService mistralService;
    @Autowired private GeminiService geminiService;
    @Autowired private NvidiaLLMService nvidiaLLMService;
    @Autowired private GeminiJudgeService geminiJudgeService;
    @Autowired private ImagenService imagenService;
    @Autowired private OpenRouterService openRouterService;

    /**
     * Crea una nueva conversación.
     *
     * @param titulo    Título de la conversación
     * @param usuarioId ID del usuario propietario
     * @return Conversación creada o null si el usuario no existe
     */
    public Conversacion crearConversacion(String titulo, long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (!usuarioOpt.isPresent()) return null;
        Conversacion conversacion = new Conversacion(titulo, usuarioOpt.get());
        return conversacionRepository.save(conversacion);
    }

    /**
     * Obtiene todas las conversaciones activas de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de conversaciones activas
     */
    public List<Conversacion> obtenerConversacionesUsuario(long usuarioId) {
        return conversacionRepository.findByUsuarioIdAndActivaTrueOrderByFechaUltimaActividadDesc(usuarioId);
    }

    /**
     * Obtiene una conversación por ID.
     *
     * @param id ID de la conversación
     * @return Optional con la conversación
     */
    public Optional<Conversacion> obtenerConversacionPorId(long id) {
        return conversacionRepository.findById(id);
    }

    /**
     * Obtiene todos los mensajes de una conversación.
     *
     * @param conversacionId ID de la conversación
     * @return Lista de mensajes ordenados cronológicamente
     */
    public List<Mensaje> obtenerMensajesConversacion(long conversacionId) {
        return mensajeRepository.findByConversacionIdOrderByFechaCreacionAsc(conversacionId);
    }

    /**
     * Envía un mensaje y genera respuestas de IA según el tipo de contenido.
     *
     * @param conversacionId ID de la conversación
     * @param contenido      Contenido del mensaje
     * @param tipoContenido  Tipo (TEXTO, IMAGEN, VIDEO)
     * @return ID del mensaje creado o -1 si la conversación no existe
     */
    public long enviarMensaje(long conversacionId, String contenido, String tipoContenido) {
        Optional<Conversacion> convOpt = conversacionRepository.findById(conversacionId);
        if (!convOpt.isPresent()) return -1;

        Conversacion conversacion = convOpt.get();
        Mensaje mensaje = new Mensaje();
        mensaje.setConversacion(conversacion);
        mensaje.setTipo(Mensaje.TipoMensaje.USUARIO);
        mensaje.setContenido(contenido);
        mensaje.setTipoContenido(Mensaje.TipoContenido.valueOf(tipoContenido));
        mensaje = mensajeRepository.save(mensaje);

        conversacion.setFechaUltimaActividad(LocalDateTime.now());
        conversacionRepository.save(conversacion);

        final Mensaje mensajeFinal = mensaje;

        if ("TEXTO".equals(tipoContenido)) {
            List<Map<String, String>> historial = construirHistorial(conversacionId, mensajeFinal.getId());
            final int totalIAs = 6;
            final Map<ModeloIA, String> respuestasRecibidas = Collections.synchronizedMap(new LinkedHashMap<>());
            final AtomicInteger contador = new AtomicInteger(0);

            Runnable onIATerminada = () -> {
                if (contador.incrementAndGet() == totalIAs) {
                    elegirYGuardarMejor(mensajeFinal, respuestasRecibidas);
                }
            };

            groqService.generarRespuesta(historial, contenido).subscribe(r -> {
                guardarRespuesta(mensajeFinal, ModeloIA.GROQ_LLAMA3, r, null);
                respuestasRecibidas.put(ModeloIA.GROQ_LLAMA3, r);
                onIATerminada.run();
            }, e -> { respuestasRecibidas.put(ModeloIA.GROQ_LLAMA3, ""); onIATerminada.run(); });

            cohereService.generarRespuesta(historial, contenido).subscribe(r -> {
                guardarRespuesta(mensajeFinal, ModeloIA.COHERE_COMMAND, r, null);
                respuestasRecibidas.put(ModeloIA.COHERE_COMMAND, r);
                onIATerminada.run();
            }, e -> { respuestasRecibidas.put(ModeloIA.COHERE_COMMAND, ""); onIATerminada.run(); });

            mistralService.generarRespuesta(historial, contenido).subscribe(r -> {
                guardarRespuesta(mensajeFinal, ModeloIA.MISTRAL_SMALL, r, null);
                respuestasRecibidas.put(ModeloIA.MISTRAL_SMALL, r);
                onIATerminada.run();
            }, e -> { respuestasRecibidas.put(ModeloIA.MISTRAL_SMALL, ""); onIATerminada.run(); });

            geminiService.generarRespuesta(historial, contenido).subscribe(r -> {
                guardarRespuesta(mensajeFinal, ModeloIA.GEMINI, r, null);
                respuestasRecibidas.put(ModeloIA.GEMINI, r);
                onIATerminada.run();
            }, e -> { respuestasRecibidas.put(ModeloIA.GEMINI, ""); onIATerminada.run(); });

            nvidiaLLMService.generarRespuesta(historial, contenido, "meta/llama-3.3-70b-instruct").subscribe(r -> {
                guardarRespuesta(mensajeFinal, ModeloIA.NVIDIA_LLAMA_3_3_70B, r, null);
                respuestasRecibidas.put(ModeloIA.NVIDIA_LLAMA_3_3_70B, r);
                onIATerminada.run();
            }, e -> { respuestasRecibidas.put(ModeloIA.NVIDIA_LLAMA_3_3_70B, ""); onIATerminada.run(); });

            nvidiaLLMService.generarRespuesta(historial, contenido, "nvidia/nemotron-3-super-120b-a12b").subscribe(r -> {
                guardarRespuesta(mensajeFinal, ModeloIA.NVIDIA_NEMOTRON_3_SUPER, r, null);
                respuestasRecibidas.put(ModeloIA.NVIDIA_NEMOTRON_3_SUPER, r);
                onIATerminada.run();
            }, e -> { respuestasRecibidas.put(ModeloIA.NVIDIA_NEMOTRON_3_SUPER, ""); onIATerminada.run(); });

        } else if ("IMAGEN".equals(tipoContenido)) {
            imagenService.generarImagenPollinations(contenido).subscribe(url ->
                guardarRespuesta(mensajeFinal, ModeloIA.POLLINATIONS_IMG, "Imagen generada por Pollinations", url));
            openRouterService.generarImagenXAI(contenido).subscribe(url ->
                guardarRespuesta(mensajeFinal, ModeloIA.OPENROUTER_XAI_GROK_IMG, "Imagen generada por xAI Grok", url));
            openRouterService.generarImagenRecraft(contenido).subscribe(url ->
                guardarRespuesta(mensajeFinal, ModeloIA.OPENROUTER_RECRAFT_IMG, "Imagen generada por Recraft", url));

        } else if ("VIDEO".equals(tipoContenido)) {
            openRouterService.generarVideoWan(contenido).subscribe(url ->
                guardarRespuesta(mensajeFinal, ModeloIA.OPENROUTER_WAN_VIDEO, "Video generado por Wan 2.6", url));
            openRouterService.generarVideoVeoLite(contenido).subscribe(url ->
                guardarRespuesta(mensajeFinal, ModeloIA.OPENROUTER_VEO_LITE_VIDEO, "Video generado por Veo 3.1 Lite", url));
            openRouterService.generarVideoXAI(contenido).subscribe(url ->
                guardarRespuesta(mensajeFinal, ModeloIA.OPENROUTER_XAI_GROK_VIDEO, "Video generado por xAI Grok", url));
        }

        return mensajeFinal.getId();
    }

    private List<Map<String, String>> construirHistorial(long conversacionId, long mensajeActualId) {
        List<Mensaje> todos = mensajeRepository.findByConversacionIdOrderByFechaCreacionAsc(conversacionId);
        List<Map<String, String>> historial = new ArrayList<>();
        int inicio = Math.max(0, todos.size() - 11);

        for (int i = inicio; i < todos.size(); i++) {
            Mensaje m = todos.get(i);
            if (m.getId() == mensajeActualId) continue;
            if (m.getTipo() != Mensaje.TipoMensaje.USUARIO) continue;
            if (m.getTipoContenido() != Mensaje.TipoContenido.TEXTO) continue;

            historial.add(Map.of("role", "user", "content", m.getContenido()));

            String mejorTexto = obtenerMejorRespuestaTexto(m);
            if (mejorTexto != null && !mejorTexto.isBlank()) {
                historial.add(Map.of("role", "assistant", "content", mejorTexto));
            }
        }
        return historial;
    }

    private String obtenerMejorRespuestaTexto(Mensaje mensaje) {
        List<RespuestaIA> respuestas = respuestaIARepository.findByMensajeIdOrderByFechaCreacionAsc(mensaje.getId());
        if (respuestas.isEmpty()) return null;

        Optional<RespuestaIA> mejor = respuestas.stream()
            .filter(RespuestaIA::isEsMejorRespuesta)
            .findFirst();
        if (mejor.isPresent()) return mejor.get().getRespuesta();

        return respuestas.stream()
            .map(RespuestaIA::getRespuesta)
            .filter(r -> r != null && !r.isBlank())
            .findFirst()
            .orElse(null);
    }

    private void elegirYGuardarMejor(Mensaje mensaje, Map<ModeloIA, String> respuestasRecibidas) {
        List<ModeloIA> modelos = new ArrayList<>();
        List<String> textos = new ArrayList<>();
        List<String> nombresModelos = new ArrayList<>();

        for (Map.Entry<ModeloIA, String> entry : respuestasRecibidas.entrySet()) {
            String r = entry.getValue();
            if (r != null && !r.isBlank() && !r.startsWith("Error")) {
                modelos.add(entry.getKey());
                textos.add(r);
                nombresModelos.add(entry.getKey().name());
            }
        }

        if (modelos.isEmpty()) return;

        geminiJudgeService.elegirMejor(mensaje.getContenido(), nombresModelos, textos)
            .subscribe(idx -> {
                if (idx < 0 || idx >= modelos.size()) return;
                ModeloIA modeloGanador = modelos.get(idx);

                List<RespuestaIA> todasRespuestas =
                    respuestaIARepository.findByMensajeIdOrderByFechaCreacionAsc(mensaje.getId());

                todasRespuestas.forEach(r -> r.setEsMejorRespuesta(false));
                respuestaIARepository.saveAll(todasRespuestas);

                todasRespuestas.stream()
                    .filter(r -> r.getModeloIA() == modeloGanador)
                    .findFirst()
                    .ifPresent(ganadora -> {
                        ganadora.setEsMejorRespuesta(true);
                        respuestaIARepository.save(ganadora);
                        mensaje.setMejorRespuestaId(ganadora.getId());
                        mensajeRepository.save(mensaje);
                    });
            }, e -> {});
    }

    private void guardarRespuesta(Mensaje mensaje, ModeloIA modelo, String respuesta, String urlArchivo) {
        RespuestaIA respuestaIA = new RespuestaIA();
        respuestaIA.setMensaje(mensaje);
        respuestaIA.setModeloIA(modelo);
        respuestaIA.setRespuesta(respuesta);
        respuestaIA.setTiempoRespuestaMs(1000L);
        respuestaIA.setUrlArchivo(urlArchivo);
        respuestaIARepository.save(respuestaIA);
    }

    /**
     * Selecciona manualmente la mejor respuesta IA para un mensaje.
     *
     * @param mensajeId     ID del mensaje
     * @param respuestaIAId ID de la respuesta a marcar como mejor
     * @return 0 si fue exitoso, 1 si el mensaje no existe
     */
    public int seleccionarMejorRespuesta(long mensajeId, long respuestaIAId) {
        Optional<Mensaje> mensajeOpt = mensajeRepository.findById(mensajeId);
        if (!mensajeOpt.isPresent()) return 1;

        List<RespuestaIA> respuestas = respuestaIARepository.findByMensajeIdOrderByFechaCreacionAsc(mensajeId);
        respuestas.forEach(r -> r.setEsMejorRespuesta(false));
        respuestaIARepository.saveAll(respuestas);

        Optional<RespuestaIA> mejorOpt = respuestaIARepository.findById(respuestaIAId);
        if (mejorOpt.isPresent()) {
            RespuestaIA mejor = mejorOpt.get();
            mejor.setEsMejorRespuesta(true);
            respuestaIARepository.save(mejor);
            Mensaje mensaje = mensajeOpt.get();
            mensaje.setMejorRespuestaId(respuestaIAId);
            mensajeRepository.save(mensaje);
        }
        return 0;
    }

    /**
     * Obtiene todas las conversaciones del sistema.
     *
     * @return Lista completa de conversaciones
     */
    public List<Conversacion> obtenerTodasConversaciones() {
        return conversacionRepository.findAll();
    }

    /**
     * Guarda una conversación en la base de datos.
     *
     * @param conversacion Conversación a guardar
     */
    public void guardarConversacion(Conversacion conversacion) {
        conversacionRepository.save(conversacion);
    }

    /**
     * Elimina una conversación y todos sus mensajes asociados.
     *
     * @param id ID de la conversación
     */
    public void eliminarConversacion(long id) {
        List<Mensaje> mensajes = mensajeRepository.findByConversacionIdOrderByFechaCreacionAsc(id);
        for (Mensaje m : mensajes) {
            respuestaIARepository.deleteByMensajeId(m.getId());
        }
        mensajeRepository.deleteByConversacionId(id);
        conversacionRepository.deleteById(id);
    }

    /**
     * Actualiza el contenido de un mensaje.
     *
     * @param mensajeId      ID del mensaje
     * @param nuevoContenido Nuevo contenido
     * @return 0 si fue exitoso, 1 si el mensaje no existe
     */
    public int actualizarMensaje(long mensajeId, String nuevoContenido) {
        Optional<Mensaje> mensajeOpt = mensajeRepository.findById(mensajeId);
        if (!mensajeOpt.isPresent()) return 1;
        Mensaje mensaje = mensajeOpt.get();
        mensaje.setContenido(nuevoContenido);
        mensajeRepository.save(mensaje);
        Conversacion conversacion = mensaje.getConversacion();
        conversacion.setFechaUltimaActividad(LocalDateTime.now());
        conversacionRepository.save(conversacion);
        return 0;
    }

    /**
     * Elimina un mensaje y sus respuestas de IA asociadas.
     *
     * @param mensajeId ID del mensaje
     * @return 0 si fue exitoso, 1 si el mensaje no existe
     */
    public int eliminarMensaje(long mensajeId) {
        Optional<Mensaje> mensajeOpt = mensajeRepository.findById(mensajeId);
        if (!mensajeOpt.isPresent()) return 1;
        Mensaje mensaje = mensajeOpt.get();
        Conversacion conversacion = mensaje.getConversacion();
        respuestaIARepository.deleteByMensajeId(mensajeId);
        mensajeRepository.deleteById(mensajeId);
        conversacion.setFechaUltimaActividad(LocalDateTime.now());
        conversacionRepository.save(conversacion);
        return 0;
    }
}
