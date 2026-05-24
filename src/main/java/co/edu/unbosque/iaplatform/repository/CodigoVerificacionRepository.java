package co.edu.unbosque.iaplatform.repository;

import co.edu.unbosque.iaplatform.entity.CodigoVerificacion;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad CodigoVerificacion.
 * Gestiona los códigos de verificación por email.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Repository
public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {

    /**
     * Busca un código de verificación activo (no usado) por email.
     *
     * @param email Email del usuario
     * @return Optional con el código si existe
     */
    Optional<CodigoVerificacion> findByEmailAndUsadoFalse(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM CodigoVerificacion c WHERE c.email = :email")
    void deleteByEmail(@Param("email") String email);
}