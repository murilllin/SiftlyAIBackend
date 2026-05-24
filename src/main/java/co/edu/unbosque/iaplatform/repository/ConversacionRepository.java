package co.edu.unbosque.iaplatform.repository;

import co.edu.unbosque.iaplatform.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para la entidad Conversacion.
 * Proporciona métodos para consultar conversaciones por usuario.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    /**
     * Obtiene todas las conversaciones de un usuario ordenadas por fecha.
     *
     * @param usuarioId ID del usuario
     * @return Lista de conversaciones ordenadas por fecha de creación descendente
     */
    List<Conversacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    /**
     * Obtiene las conversaciones activas de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de conversaciones activas ordenadas por última actividad
     */
    List<Conversacion> findByUsuarioIdAndActivaTrueOrderByFechaUltimaActividadDesc(Long usuarioId);
}