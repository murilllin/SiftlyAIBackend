package co.edu.unbosque.iaplatform.repository;

import co.edu.unbosque.iaplatform.entity.RespuestaIA;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para la entidad RespuestaIA.
 * Gestiona las respuestas generadas por los diferentes modelos de IA.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Repository
public interface RespuestaIARepository extends JpaRepository<RespuestaIA, Long> {

    /**
     * Obtiene todas las respuestas de IA para un mensaje específico.
     *
     * @param mensajeId ID del mensaje
     * @return Lista de respuestas ordenadas por fecha de creación ascendente
     */
    List<RespuestaIA> findByMensajeIdOrderByFechaCreacionAsc(Long mensajeId);

    /**
     * Elimina todas las respuestas de IA asociadas a un mensaje.
     *
     * @param mensajeId ID del mensaje
     */
    @Transactional
    void deleteByMensajeId(Long mensajeId);
}