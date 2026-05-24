package co.edu.unbosque.iaplatform.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferir conversaciones al frontend. Incluye los mensajes
 * asociados a la conversación.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
public class ConversacionDTO {

	private long id;
	private String titulo;
	private long usuarioId;
	private String usuarioNombre;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaUltimaActividad;
	private boolean activa;
	private List<MensajeDTO> mensajes;

	public ConversacionDTO() {
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

	public long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public String getUsuarioNombre() {
		return usuarioNombre;
	}

	public void setUsuarioNombre(String usuarioNombre) {
		this.usuarioNombre = usuarioNombre;
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

	public List<MensajeDTO> getMensajes() {
		return mensajes;
	}

	public void setMensajes(List<MensajeDTO> mensajes) {
		this.mensajes = mensajes;
	}
}