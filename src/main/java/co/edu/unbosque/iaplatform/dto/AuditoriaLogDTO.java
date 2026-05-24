package co.edu.unbosque.iaplatform.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir logs de auditoría al frontend. Contiene toda la
 * información relevante de una acción registrada.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
public class AuditoriaLogDTO {

	private long id;
	private String usuarioEmail;
	private String usuarioNombre;
	private String accion;
	private String detalle;
	private String ipAddress;
	private LocalDateTime fecha;
	private boolean exitoso;

	public AuditoriaLogDTO() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
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