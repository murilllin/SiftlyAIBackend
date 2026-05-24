package co.edu.unbosque.iaplatform.service;

import co.edu.unbosque.iaplatform.entity.Mensaje;
import co.edu.unbosque.iaplatform.repository.MensajeRepository;
import co.edu.unbosque.iaplatform.repository.RespuestaIARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones CRUD de mensajes.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private RespuestaIARepository respuestaIARepository;

    /**
     * Obtiene todos los mensajes de una conversación.
     *
     * @param conversacionId ID de la conversación
     * @return Lista de mensajes ordenados por fecha
     */
    public List<Mensaje> obtenerMensajesPorConversacion(long conversacionId) {
        return mensajeRepository.findByConversacionIdOrderByFechaCreacionAsc(conversacionId);
    }

    /**
     * Obtiene un mensaje por ID.
     *
     * @param id ID del mensaje
     * @return Optional con el mensaje
     */
    public Optional<Mensaje> obtenerMensajePorId(long id) {
        return mensajeRepository.findById(id);
    }

    /**
     * Elimina un mensaje y sus respuestas asociadas.
     *
     * @param id ID del mensaje
     */
    public void eliminarMensaje(long id) {
        respuestaIARepository.deleteByMensajeId(id);
        mensajeRepository.deleteById(id);
    }
}