package co.edu.unbosque.iaplatform.repository;

import co.edu.unbosque.iaplatform.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Repositorio para la entidad Mensaje.
 * Proporciona métodos para consultar mensajes por conversación.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /**
     * Obtiene todos los mensajes de una conversación ordenados cronológicamente.
     *
     * @param conversacionId ID de la conversación
     * @return Lista de mensajes ordenados por fecha de creación ascendente
     */
    List<Mensaje> findByConversacionIdOrderByFechaCreacionAsc(Long conversacionId);

    /**
     * Elimina todos los mensajes de una conversación.
     *
     * @param conversacionId ID de la conversación
     */
    @Transactional
    void deleteByConversacionId(Long conversacionId);
}