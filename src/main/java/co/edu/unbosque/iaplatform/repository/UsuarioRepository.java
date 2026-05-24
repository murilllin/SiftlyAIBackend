package co.edu.unbosque.iaplatform.repository;

import co.edu.unbosque.iaplatform.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 * Proporciona métodos para búsqueda por email y estadísticas.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su email.
     *
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario con el email dado.
     *
     * @param email Email a verificar
     * @return true si el email ya está registrado
     */
    boolean existsByEmail(String email);

    /**
     * Cuenta el número de usuarios activos en el sistema.
     *
     * @return Cantidad de usuarios con activo = true
     */
    long countByActivoTrue();
}