package co.edu.unbosque.iaplatform.controller;

import co.edu.unbosque.iaplatform.entity.AuditoriaLog.TipoAccion;
import co.edu.unbosque.iaplatform.entity.Conversacion;
import co.edu.unbosque.iaplatform.entity.Mensaje;
import co.edu.unbosque.iaplatform.entity.RespuestaIA;
import co.edu.unbosque.iaplatform.entity.Usuario;
import co.edu.unbosque.iaplatform.repository.RespuestaIARepository;
import co.edu.unbosque.iaplatform.repository.UsuarioRepository;
import co.edu.unbosque.iaplatform.service.AuditoriaService;
import co.edu.unbosque.iaplatform.service.ConversacionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador para gestión de conversaciones y mensajes.
 * <p>
 * Permite crear conversaciones, enviar mensajes, obtener respuestas de IA
 * y seleccionar la mejor respuesta generada por los diferentes modelos.
 * </p>
 * 
 * @author Daniel Murillo
 * @version 1.0
 */
@RestController
@RequestMapping("/conversaciones")
@CrossOrigin(originPatterns = "*")
public class ConversacionController {

    @Autowired private ConversacionService conversacionService;
    @Autowired private RespuestaIARepository respuestaIARepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AuditoriaService auditoriaService;

    /**
     * Crea una nueva conversación.
     *
     * @param request Mapa con titulo y usuarioId
     * @param httpReq HttpServletRequest para auditoría
     * @return Conversación creada
     */
    @PostMapping
    public ResponseEntity<?> crearConversacion(@RequestBody Map<String, Object> request,
                                               HttpServletRequest httpReq) {
        String titulo    = (String) request.get("titulo");
        long   usuarioId = Long.parseLong(request.get("usuarioId").toString());
        Conversacion conv = conversacionService.crearConversacion(titulo, usuarioId);
        if (conv == null) return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));

        Optional<Usuario> u = usuarioRepository.findById(usuarioId);
        auditoriaService.registrar(u.orElse(null), TipoAccion.CREAR_CONVERSACION,
            "Conversación creada: '" + titulo + "' (id=" + conv.getId() + ")", getIp(httpReq));

        return ResponseEntity.ok(Map.of(
            "id", conv.getId(), "titulo", conv.getTitulo(),
            "fechaCreacion", conv.getFechaCreacion().toString(), "activa", conv.isActiva()
        ));
    }

    /**
     * Obtiene todas las conversaciones de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de conversaciones
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerConversacionesUsuario(@PathVariable long usuarioId) {
        List<Conversacion> conversaciones = conversacionService.obtenerConversacionesUsuario(usuarioId);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Conversacion conv : conversaciones) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", conv.getId());
            dto.put("titulo", conv.getTitulo());
            dto.put("fechaCreacion", conv.getFechaCreacion());
            dto.put("fechaUltimaActividad", conv.getFechaUltimaActividad());
            dto.put("activa", conv.isActiva());
            response.add(dto);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene una conversación con todos sus mensajes y respuestas de IA.
     *
     * @param id     ID de la conversación
     * @param httpReq HttpServletRequest para auditoría
     * @return Conversación completa con mensajes y respuestas
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerConversacion(@PathVariable long id,
                                                  HttpServletRequest httpReq) {
        Optional<Conversacion> convOpt = conversacionService.obtenerConversacionPorId(id);
        if (!convOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Conversación no encontrada"));
        }
        Conversacion conv = convOpt.get();

        Usuario usuarioConv = conv.getUsuario();
        String emailConv  = usuarioConv != null ? usuarioConv.getEmail()  : "desconocido";
        String nombreConv = usuarioConv != null
            ? usuarioConv.getNombre() + " " + usuarioConv.getApellido()
            : "desconocido";
        auditoriaService.registrar(emailConv, nombreConv, TipoAccion.VER_CONVERSACION,
            "Vista conversación: '" + conv.getTitulo() + "' (id=" + id + ")", getIp(httpReq));
        
        List<Mensaje> mensajes = conversacionService.obtenerMensajesConversacion(id);

        List<Map<String, Object>> mensajesDTO = new ArrayList<>();
        for (Mensaje mensaje : mensajes) {
            Map<String, Object> mensajeDTO = new HashMap<>();
            mensajeDTO.put("id", mensaje.getId());
            mensajeDTO.put("tipo", mensaje.getTipo().name());
            mensajeDTO.put("contenido", mensaje.getContenido());
            mensajeDTO.put("tipoContenido", mensaje.getTipoContenido().name());
            mensajeDTO.put("fechaCreacion", mensaje.getFechaCreacion());

            List<RespuestaIA> respuestas = respuestaIARepository
                .findByMensajeIdOrderByFechaCreacionAsc(mensaje.getId());
            List<Map<String, Object>> respuestasDTO = new ArrayList<>();
            for (RespuestaIA resp : respuestas) {
                Map<String, Object> respDTO = new HashMap<>();
                respDTO.put("id", resp.getId());
                respDTO.put("modeloIA", resp.getModeloIA().name());
                respDTO.put("nombreModelo", resp.getModeloIA().name());
                respDTO.put("respuesta", resp.getRespuesta());
                respDTO.put("tiempoRespuestaMs", resp.getTiempoRespuestaMs());
                respDTO.put("esMejorRespuesta", resp.isEsMejorRespuesta());
                respDTO.put("urlArchivo", resp.getUrlArchivo());
                respuestasDTO.add(respDTO);
            }
            mensajeDTO.put("respuestasIA", respuestasDTO);
            mensajesDTO.add(mensajeDTO);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", conv.getId());
        response.put("titulo", conv.getTitulo());
        response.put("mensajes", mensajesDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Envía un mensaje y genera respuestas de múltiples modelos de IA.
     *
     * @param id      ID de la conversación
     * @param request Mapa con contenido y tipoContenido
     * @param httpReq HttpServletRequest para auditoría
     * @return ID del mensaje creado
     */
    @PostMapping("/{id}/mensajes")
    public ResponseEntity<?> enviarMensaje(@PathVariable long id,
                                           @RequestBody Map<String, String> request,
                                           HttpServletRequest httpReq) {
        String contenido      = request.get("contenido");
        String tipoContenido  = request.getOrDefault("tipoContenido", "TEXTO");
        long mensajeId = conversacionService.enviarMensaje(id, contenido, tipoContenido);
        if (mensajeId >= 0) {
            Optional<Conversacion> convOpt = conversacionService.obtenerConversacionPorId(id);
            convOpt.ifPresent(c -> {
                Usuario u = c.getUsuario();
                auditoriaService.registrar(
                    u != null ? u.getEmail() : "desconocido",
                    u != null ? u.getNombre() + " " + u.getApellido() : "desconocido",
                    TipoAccion.ENVIAR_MENSAJE,
                    "Mensaje en conv id=" + id + " tipo=" + tipoContenido, getIp(httpReq));
            });
            return ResponseEntity.ok(Map.of("success", true, "mensajeId", mensajeId));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Conversación no encontrada"));
    }

    /**
     * Selecciona manualmente la mejor respuesta IA para un mensaje.
     *
     * @param mensajeId ID del mensaje
     * @param request   Mapa con respuestaIAId
     * @return Confirmación de éxito
     */
    @PostMapping("/mensajes/{mensajeId}/mejor-respuesta")
    public ResponseEntity<?> seleccionarMejorRespuesta(@PathVariable long mensajeId,
                                                        @RequestBody Map<String, Long> request) {
        long respuestaIAId = request.get("respuestaIAId");
        int resultado = conversacionService.seleccionarMejorRespuesta(mensajeId, respuestaIAId);
        if (resultado == 0) return ResponseEntity.ok(Map.of("success", true));
        return ResponseEntity.badRequest().body(Map.of("error", "Mensaje no encontrado"));
    }

    /**
     * Lista todas las conversaciones del sistema (para administración).
     *
     * @return Lista completa de conversaciones
     */
    @GetMapping
    public ResponseEntity<?> listarTodasConversaciones() {
        List<Conversacion> conversaciones = conversacionService.obtenerTodasConversaciones();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Conversacion conv : conversaciones) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", conv.getId());
            dto.put("titulo", conv.getTitulo());
            dto.put("usuarioId", conv.getUsuario().getId());
            dto.put("usuarioNombre", conv.getUsuario().getNombre());
            dto.put("fechaCreacion", conv.getFechaCreacion());
            dto.put("fechaUltimaActividad", conv.getFechaUltimaActividad());
            dto.put("activa", conv.isActiva());
            response.add(dto);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza el título de una conversación.
     *
     * @param id      ID de la conversación
     * @param request Mapa con nuevo titulo
     * @return Conversación actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarConversacion(@PathVariable long id,
                                                     @RequestBody Map<String, String> request) {
        Optional<Conversacion> convOpt = conversacionService.obtenerConversacionPorId(id);
        if (!convOpt.isPresent()) return ResponseEntity.notFound().build();
        Conversacion conversacion = convOpt.get();
        String nuevoTitulo = request.get("titulo");
        if (nuevoTitulo != null && !nuevoTitulo.isEmpty()) conversacion.setTitulo(nuevoTitulo);
        conversacionService.guardarConversacion(conversacion);
        return ResponseEntity.ok(Map.of("success", true, "titulo", conversacion.getTitulo()));
    }

    /**
     * Elimina una conversación y todos sus mensajes asociados.
     *
     * @param id     ID de la conversación
     * @param httpReq HttpServletRequest para auditoría
     * @return Confirmación de éxito
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarConversacion(@PathVariable long id,
                                                   HttpServletRequest httpReq) {
        Optional<Conversacion> convOpt = conversacionService.obtenerConversacionPorId(id);
        if (!convOpt.isPresent()) return ResponseEntity.notFound().build();
        Conversacion conv = convOpt.get();
        Usuario uConv = conv.getUsuario();
        auditoriaService.registrar(
            uConv != null ? uConv.getEmail() : "desconocido",
            uConv != null ? uConv.getNombre() + " " + uConv.getApellido() : "desconocido",
            TipoAccion.ELIMINAR_CONVERSACION,
            "Conversación eliminada: '" + conv.getTitulo() + "' (id=" + id + ")", getIp(httpReq));
        conversacionService.eliminarConversacion(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Conversación eliminada"));
    }

    /**
     * Activa o desactiva una conversación.
     *
     * @param id      ID de la conversación
     * @param request Mapa con campo "activa"
     * @return Nuevo estado de la conversación
     */
    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> toggleConversacionActiva(@PathVariable long id,
                                                       @RequestBody Map<String, Boolean> request) {
        Optional<Conversacion> convOpt = conversacionService.obtenerConversacionPorId(id);
        if (!convOpt.isPresent()) return ResponseEntity.notFound().build();
        Conversacion conversacion = convOpt.get();
        conversacion.setActiva(request.getOrDefault("activa", true));
        conversacionService.guardarConversacion(conversacion);
        return ResponseEntity.ok(Map.of("success", true, "activa", conversacion.isActiva()));
    }

    /**
     * Actualiza el contenido de un mensaje.
     *
     * @param mensajeId ID del mensaje
     * @param request   Mapa con nuevo contenido
     * @return Confirmación de éxito
     */
    @PutMapping("/mensajes/{mensajeId}")
    public ResponseEntity<?> actualizarMensaje(@PathVariable long mensajeId,
                                                @RequestBody Map<String, String> request) {
        String nuevoContenido = request.get("contenido");
        if (nuevoContenido == null || nuevoContenido.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El contenido no puede estar vacío"));
        }
        int resultado = conversacionService.actualizarMensaje(mensajeId, nuevoContenido);
        if (resultado == 0) return ResponseEntity.ok(Map.of("success", true, "message", "Mensaje actualizado"));
        return ResponseEntity.badRequest().body(Map.of("error", "Mensaje no encontrado"));
    }

    /**
     * Elimina un mensaje y sus respuestas de IA asociadas.
     *
     * @param mensajeId ID del mensaje
     * @return Confirmación de éxito
     */
    @DeleteMapping("/mensajes/{mensajeId}")
    public ResponseEntity<?> eliminarMensaje(@PathVariable long mensajeId) {
        int resultado = conversacionService.eliminarMensaje(mensajeId);
        if (resultado == 0) return ResponseEntity.ok(Map.of("success", true, "message", "Mensaje eliminado"));
        return ResponseEntity.badRequest().body(Map.of("error", "Mensaje no encontrado"));
    }

    private String getIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim() : req.getRemoteAddr();
    }
}