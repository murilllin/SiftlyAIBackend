package co.edu.unbosque.iaplatform.controller;

import co.edu.unbosque.iaplatform.dto.AuditoriaLogDTO;
import co.edu.unbosque.iaplatform.dto.UsuarioDTO;
import co.edu.unbosque.iaplatform.entity.AuditoriaLog;
import co.edu.unbosque.iaplatform.entity.AuditoriaLog.TipoAccion;
import co.edu.unbosque.iaplatform.entity.Usuario;
import co.edu.unbosque.iaplatform.service.AdminService;
import co.edu.unbosque.iaplatform.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador para el panel de administración.
 * <p>
 * Todos los endpoints requieren rol ADMIN.
 * Proporciona funcionalidades para gestionar usuarios, ver estadísticas
 * y consultar la auditoría del sistema.
 * </p>
 * 
 * @author Daniel Murillo
 * @version 1.0
 */
@RestController("adminPanelController")
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private AuditoriaService auditoriaService;

    /**
     * Obtiene todos los usuarios del sistema.
     *
     * @return Lista de usuarios en formato DTO
     */
    @GetMapping("/usuarios")
    public ResponseEntity<?> obtenerTodosUsuarios() {
        List<Usuario> usuarios = adminService.obtenerTodosUsuarios();
        List<UsuarioDTO> response = new ArrayList<>();
        for (Usuario u : usuarios) {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(u.getId());
            dto.setEmail(u.getEmail());
            dto.setNombre(u.getNombre());
            dto.setApellido(u.getApellido());
            dto.setRol(u.getRol());
            dto.setActivo(u.isActivo());
            response.add(dto);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene estadísticas generales del sistema.
     *
     * @param httpReq HttpServletRequest para obtener IP
     * @param auth    Autenticación del administrador
     * @return Mapa con totalUsuarios, usuariosActivos, totalConversaciones, totalMensajes
     */
    @GetMapping("/stats")
    public ResponseEntity<?> obtenerEstadisticas(HttpServletRequest httpReq,
            org.springframework.security.core.Authentication auth) {
        Map<String, Object> stats = adminService.obtenerEstadisticas();

        if (auth != null) {
            auditoriaService.registrar(null, TipoAccion.VER_PANEL_ADMIN,
                "Admin panel accedido por: " + auth.getName(), getIp(httpReq));
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * Cambia el rol de un usuario.
     *
     * @param id     ID del usuario
     * @param req    Mapa con el nuevo rol (campo "rol")
     * @param httpReq HttpServletRequest para auditoría
     * @param auth   Autenticación del administrador
     * @return Respuesta de éxito o error
     */
    @PatchMapping("/usuarios/{id}/rol")
    public ResponseEntity<?> cambiarRol(@PathVariable long id,
                                        @RequestBody Map<String, String> req,
                                        HttpServletRequest httpReq,
                                        org.springframework.security.core.Authentication auth) {
        String nuevoRol = req.get("rol");
        if (nuevoRol == null || nuevoRol.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rol requerido"));
        }
        adminService.cambiarRol(id, nuevoRol.toUpperCase());

        auditoriaService.registrar(null, TipoAccion.CAMBIAR_ROL,
            "Rol cambiado a '" + nuevoRol.toUpperCase() + "' para usuarioId=" + id
                + (auth != null ? " por admin: " + auth.getName() : ""),
            getIp(httpReq));

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Obtiene los logs de auditoría paginados y filtrados.
     *
     * @param pagina Número de página (default 0)
     * @param tamano Tamaño de página (default 50)
     * @param accion Filtrar por tipo de acción (opcional)
     * @param desde  Fecha de inicio (opcional, ISO DateTime)
     * @param hasta  Fecha de fin (opcional, ISO DateTime)
     * @return Página de logs de auditoría
     */
    @GetMapping("/auditoria")
    public ResponseEntity<?> obtenerAuditoria(
            @RequestParam(defaultValue = "0")   int pagina,
            @RequestParam(name = "tamano", defaultValue = "50") int tamano,
            @RequestParam(required = false)     String accion,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        Page<AuditoriaLog> page = auditoriaService.buscar(accion, desde, hasta, pagina, tamano);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("contenido",      page.getContent().stream().map(this::toDTO).toList());
        res.put("paginaActual",   page.getNumber());
        res.put("totalPaginas",   page.getTotalPages());
        res.put("totalElementos", page.getTotalElements());
        return ResponseEntity.ok(res);
    }

    /**
     * Obtiene los logs de auditoría de un usuario específico.
     *
     * @param id ID del usuario
     * @return Lista de logs del usuario
     */
    @GetMapping("/auditoria/usuario/{id}")
    public ResponseEntity<?> auditoriaUsuario(@PathVariable Long id) {
        List<AuditoriaLog> logs = auditoriaService.obtenerLogsPorUsuario(id);
        return ResponseEntity.ok(logs.stream().map(this::toDTO).toList());
    }

    private AuditoriaLogDTO toDTO(AuditoriaLog l) {
        AuditoriaLogDTO dto = new AuditoriaLogDTO();
        dto.setId(l.getId());
        dto.setUsuarioEmail(l.getUsuarioEmail());
        dto.setUsuarioNombre(l.getUsuarioNombre());
        dto.setAccion(l.getAccion() != null ? l.getAccion().name() : null);
        dto.setDetalle(l.getDetalle());
        dto.setIpAddress(l.getIpAddress());
        dto.setFecha(l.getFecha());
        dto.setExitoso(l.isExitoso());
        return dto;
    }

    private String getIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim() : req.getRemoteAddr();
    }
}
