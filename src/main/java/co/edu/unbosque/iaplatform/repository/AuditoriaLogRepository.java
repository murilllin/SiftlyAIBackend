package co.edu.unbosque.iaplatform.repository;

import co.edu.unbosque.iaplatform.entity.AuditoriaLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad AuditoriaLog.
 * Proporciona métodos para consultar logs con filtros y paginación.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {

    /**
     * Obtiene todos los logs ordenados por fecha descendente.
     *
     * @param pageable Configuración de paginación
     * @return Página de logs
     */
    Page<AuditoriaLog> findAllByOrderByFechaDesc(Pageable pageable);

    /**
     * Obtiene los logs de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return Lista de logs del usuario ordenados por fecha descendente
     */
    List<AuditoriaLog> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    /**
     * Búsqueda avanzada de logs con filtros opcionales.
     *
     * @param accion   Tipo de acción (opcional)
     * @param desde    Fecha de inicio (opcional)
     * @param hasta    Fecha de fin (opcional)
     * @param pageable Configuración de paginación
     * @return Página de logs que coinciden con los filtros
     */
    @Query("SELECT a FROM AuditoriaLog a WHERE " +
           "(:accion IS NULL OR a.accion = :accion) AND " +
           "(:desde IS NULL OR a.fecha >= :desde) AND " +
           "(:hasta IS NULL OR a.fecha <= :hasta) " +
           "ORDER BY a.fecha DESC")
    Page<AuditoriaLog> buscar(
        @Param("accion") AuditoriaLog.TipoAccion accion,
        @Param("desde")  LocalDateTime desde,
        @Param("hasta")  LocalDateTime hasta,
        Pageable pageable
    );
}