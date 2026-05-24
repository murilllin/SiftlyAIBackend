package co.edu.unbosque.iaplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una conversación entre un usuario y la IA. Contiene
 * múltiples mensajes y mantiene track de la última actividad.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Entity
@Table(name = "conversaciones54")
public class Conversacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false, length = 200)
	private String titulo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "fecha_ultima_actividad")
	private LocalDateTime fechaUltimaActividad;

	@Column(nullable = false)
	private boolean activa;

	public Conversacion() {
		this.fechaCreacion = LocalDateTime.now();
		this.fechaUltimaActividad = LocalDateTime.now();
		this.activa = true;
	}

	/**
	 * Constructor que crea una conversación con título y usuario.
	 *
	 * @param titulo  Título de la conversación
	 * @param usuario Usuario propietario
	 */
	public Conversacion(String titulo, Usuario usuario) {
		this.titulo = titulo;
		this.usuario = usuario;
		this.fechaCreacion = LocalDateTime.now();
		this.fechaUltimaActividad = LocalDateTime.now();
		this.activa = true;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public LocalDateTime getFechaUltimaActividad() {
		return fechaUltimaActividad;
	}

	public void setFechaUltimaActividad(LocalDateTime fechaUltimaActividad) {
		this.fechaUltimaActividad = fechaUltimaActividad;
	}

	public boolean isActiva() {
		return activa;
	}

	public void setActiva(boolean activa) {
		this.activa = activa;
	}
}