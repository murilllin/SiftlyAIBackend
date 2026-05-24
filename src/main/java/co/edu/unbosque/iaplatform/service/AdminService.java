package co.edu.unbosque.iaplatform.service;

import co.edu.unbosque.iaplatform.entity.Usuario;
import co.edu.unbosque.iaplatform.repository.ConversacionRepository;
import co.edu.unbosque.iaplatform.repository.MensajeRepository;
import co.edu.unbosque.iaplatform.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para operaciones administrativas del sistema.
 * Proporciona métodos para gestionar usuarios y obtener estadísticas.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class AdminService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ConversacionRepository conversacionRepository;
    @Autowired private MensajeRepository mensajeRepository;

    /**
     * Obtiene todos los usuarios del sistema.
     *
     * @return Lista completa de usuarios
     */
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene estadísticas generales del sistema.
     *
     * @return Mapa con totalUsuarios, usuariosActivos, totalConversaciones, totalMensajes
     */
    public Map<String, Object> obtenerEstadisticas() {
        long totalUsuarios = usuarioRepository.count();
        long usuariosActivos = usuarioRepository.countByActivoTrue();
        long totalConversaciones = conversacionRepository.count();
        long totalMensajes = mensajeRepository.count();

        return Map.of(
            "totalUsuarios", totalUsuarios,
            "usuariosActivos", usuariosActivos,
            "totalConversaciones", totalConversaciones,
            "totalMensajes", totalMensajes
        );
    }

    /**
     * Cambia el rol de un usuario.
     *
     * @param usuarioId ID del usuario
     * @param nuevoRol  Nuevo rol (ADMIN o USUARIO)
     */
    public void cambiarRol(long usuarioId, String nuevoRol) {
        Optional<Usuario> opt = usuarioRepository.findById(usuarioId);
        if (opt.isPresent()) {
            Usuario u = opt.get();
            u.setRol(nuevoRol);
            usuarioRepository.save(u);
        }
    }
}