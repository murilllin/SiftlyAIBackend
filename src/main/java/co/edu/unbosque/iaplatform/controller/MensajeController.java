package co.edu.unbosque.iaplatform.controller;

import co.edu.unbosque.iaplatform.entity.Mensaje;
import co.edu.unbosque.iaplatform.service.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * Controlador para operaciones específicas sobre mensajes.
 * <p>
 * Proporciona endpoints para listar mensajes de una conversación,
 * obtener un mensaje por ID y eliminarlo.
 * </p>
 * 
 * @author Daniel Murillo
 * @version 1.0
 */
@RestController
@RequestMapping("/mensajes")
@CrossOrigin(originPatterns = "*")
public class MensajeController {
    
    @Autowired
    private MensajeService mensajeService;
    
    /**
     * Lista todos los mensajes de una conversación.
     *
     * @param conversacionId ID de la conversación
     * @return Lista de mensajes ordenados por fecha
     */
    @GetMapping("/conversacion/{conversacionId}")
    public ResponseEntity<?> listarMensajesPorConversacion(@PathVariable long conversacionId) {
        List<Mensaje> mensajes = mensajeService.obtenerMensajesPorConversacion(conversacionId);
        return ResponseEntity.ok(mensajes);
    }
    
    /**
     * Obtiene un mensaje por su ID.
     *
     * @param id ID del mensaje
     * @return Mensaje encontrado o 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMensaje(@PathVariable long id) {
        Optional<Mensaje> mensajeOpt = mensajeService.obtenerMensajePorId(id);
        if (!mensajeOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mensajeOpt.get());
    }
    
    /**
     * Elimina un mensaje por su ID.
     *
     * @param id ID del mensaje
     * @return Confirmación de éxito
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMensaje(@PathVariable long id) {
        mensajeService.eliminarMensaje(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Mensaje eliminado"));
    }
}