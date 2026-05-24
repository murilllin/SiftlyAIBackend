package co.edu.unbosque.iaplatform.service;

import co.edu.unbosque.iaplatform.entity.AuditoriaLog;
import co.edu.unbosque.iaplatform.entity.AuditoriaLog.TipoAccion;
import co.edu.unbosque.iaplatform.entity.Usuario;
import co.edu.unbosque.iaplatform.repository.AuditoriaLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para registro de auditoría.
 * Permite registrar acciones exitosas y fallidas, y consultar logs con filtros.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaLogRepository auditoriaLogRepository;

    /**
     * Registra una acción exitosa con email y nombre en texto plano.
     * Usado cuando el Usuario ya no está disponible como entidad.
     *
     * @param email  Email del usuario
     * @param nombre Nombre completo del usuario
     * @param accion Tipo de acción realizada
     * @param detalle Descripción detallada
     * @param ip     Dirección IP del cliente
     */
    public void registrar(String email, String nombre, TipoAccion accion, String detalle, String ip) {
        AuditoriaLog log = new AuditoriaLog();
        log.setUsuarioEmail(email);
        log.setUsuarioNombre(nombre);
        log.setAccion(accion);
        log.setDetalle(detalle);
        log.setIpAddress(ip);
        log.setExitoso(true);
        auditoriaLogRepository.save(log);
    }

    /**
     * Registra una acción exitosa.
     *
     * @param usuario Usuario que realizó la acción
     * @param accion  Tipo de acción
     * @param detalle Descripción detallada
     * @param ip      Dirección IP del cliente
     */
    public void registrar(Usuario usuario, TipoAccion accion, String detalle, String ip) {
        AuditoriaLog log = new AuditoriaLog(usuario, accion, detalle, ip);
        log.setExitoso(true);
        auditoriaLogRepository.save(log);
    }

    /**
     * Registra una acción fallida.
     *
     * @param usuario Usuario que intentó la acción
     * @param accion  Tipo de acción
     * @param detalle Descripción del fallo
     * @param ip      Dirección IP del cliente
     */
    public void registrarFallo(Usuario usuario, TipoAccion accion, String detalle, String ip) {
        AuditoriaLog log = new AuditoriaLog(usuario, accion, detalle, ip);
        log.setExitoso(false);
        auditoriaLogRepository.save(log);
    }

    /**
     * Búsqueda paginada de logs con filtros opcionales.
     *
     * @param accion Filtrar por tipo de acción (opcional)
     * @param desde  Fecha de inicio (opcional)
     * @param hasta  Fecha de fin (opcional)
     * @param pagina Número de página
     * @param tamano Tamaño de página
     * @return Página de logs que coinciden con los filtros
     */
    public Page<AuditoriaLog> buscar(String accion, LocalDateTime desde,
                                     LocalDateTime hasta, int pagina, int tamano) {
        TipoAccion tipoAccion = null;
        if (accion != null && !accion.isBlank()) {
            try {
                tipoAccion = TipoAccion.valueOf(accion.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        PageRequest pageRequest = PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "fecha"));
        return auditoriaLogRepository.buscar(tipoAccion, desde, hasta, pageRequest);
    }

    /**
     * Obtiene todos los logs de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return Lista de logs ordenados por fecha descendente
     */
    public List<AuditoriaLog> obtenerLogsPorUsuario(Long usuarioId) {
        return auditoriaLogRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }
}