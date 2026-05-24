package co.edu.unbosque.iaplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Entidad que registra todas las acciones de auditoría del sistema. Almacena
 * quién, cuándo, qué acción y desde qué IP se realizó.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Entity
@Table(name = "auditoria_logs54")
public class AuditoriaLog {

	public enum TipoAccion {
		LOGIN, LOGOUT, CREAR_CONVERSACION, ELIMINAR_CONVERSACION, ENVIAR_MENSAJE, CAMBIAR_ROL, ACTIVAR_USUARIO,
		DESACTIVAR_USUARIO, ELIMINAR_USUARIO, VER_CONVERSACION, VER_PANEL_ADMIN, REGISTRO, VERIFICACION_CODIGO,
		OAUTH2_LOGIN
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Usuario usuario;

	@Column(name = "usuario_email", length = 300)
	private String usuarioEmail;

	@Column(name = "usuario_nombre", length = 200)
	private String usuarioNombre;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private TipoAccion accion;

	@Column(columnDefinition = "TEXT")
	private String detalle;

	@Column(name = "ip_address", length = 60)
	private String ipAddress;

	@Column(name = "fecha", nullable = false)
	private LocalDateTime fecha;

	@Column(name = "exitoso", nullable = false)
	private boolean exitoso = true;

	public AuditoriaLog() {
		this.fecha = LocalDateTime.now();
	}

	/**
	 * Constructor que inicializa el log con los datos del usuario.
	 *
	 * @param usuario Usuario que realizó la acción
	 * @param accion  Tipo de acción realizada
	 * @param detalle Descripción detallada de la acción
	 * @param ip      Dirección IP del cliente
	 */
	public AuditoriaLog(Usuario usuario, TipoAccion accion, String detalle, String ip) {
		this.fecha = LocalDateTime.now();
		this.usuario = usuario;
		this.usuarioEmail = usuario != null ? usuario.getEmail() : "desconocido";
		this.usuarioNombre = usuario != null ? usuario.getNombre() + " " + usuario.getApellido() : "desconocido";
		this.accion = accion;
		this.detalle = detalle;
		this.ipAddress = ip;
		this.exitoso = true;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getUsuarioEmail() {
		return usuarioEmail;
	}

	public void setUsuarioEmail(String usuarioEmail) {
		this.usuarioEmail = usuarioEmail;
	}

	public String getUsuarioNombre() {
		return usuarioNombre;
	}

	public void setUsuarioNombre(String usuarioNombre) {
		this.usuarioNombre = usuarioNombre;
	}

	public TipoAccion getAccion() {
		return accion;
	}

	public void setAccion(TipoAccion accion) {
		this.accion = accion;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public boolean isExitoso() {
		return exitoso;
	}

	public void setExitoso(boolean exitoso) {
		this.exitoso = exitoso;
	}
}