package co.edu.unbosque.iaplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que almacena códigos de verificación para registro de usuarios. Los
 * códigos expiran después de un tiempo configurable.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Entity
@Table(name = "codigos_verificacion54")
public class CodigoVerificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 6)
	private String codigo;

	@Column(name = "expira_en", nullable = false)
	private LocalDateTime expiraEn;

	@Column(nullable = false)
	private boolean usado;

	public CodigoVerificacion() {
	}

	/**
	 * Constructor que crea un código de verificación con fecha de expiración.
	 *
	 * @param email             Email del usuario
	 * @param codigo            Código de 6 dígitos
	 * @param minutosExpiracion Minutos hasta que expira el código
	 */
	public CodigoVerificacion(String email, String codigo, int minutosExpiracion) {
		this.email = email;
		this.codigo = codigo;
		this.expiraEn = LocalDateTime.now().plusMinutes(minutosExpiracion);
		this.usado = false;
	}

	/**
	 * Verifica si el código ya expiró.
	 *
	 * @return true si la fecha actual es posterior a la expiración
	 */
	public boolean estaExpirado() {
		return LocalDateTime.now().isAfter(expiraEn);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public LocalDateTime getExpiraEn() {
		return expiraEn;
	}

	public void setExpiraEn(LocalDateTime expiraEn) {
		this.expiraEn = expiraEn;
	}

	public boolean isUsado() {
		return usado;
	}

	public void setUsado(boolean usado) {
		this.usado = usado;
	}
}