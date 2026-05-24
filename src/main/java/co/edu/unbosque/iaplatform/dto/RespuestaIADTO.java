package co.edu.unbosque.iaplatform.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir respuestas de IA al frontend. Incluye el modelo que
 * generó la respuesta y metadata.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
public class RespuestaIADTO {

	private long id;
	private long mensajeId;
	private String modeloIA;
	private String nombreModelo;
	private String respuesta;
	private Long tiempoRespuestaMs;
	private boolean esMejorRespuesta;
	private LocalDateTime fechaCreacion;
	private String urlArchivo;

	public RespuestaIADTO() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMensajeId() {
		return mensajeId;
	}

	public void setMensajeId(long mensajeId) {
		this.mensajeId = mensajeId;
	}

	public String getModeloIA() {
		return modeloIA;
	}

	public void setModeloIA(String modeloIA) {
		this.modeloIA = modeloIA;
	}

	public String getNombreModelo() {
		return nombreModelo;
	}

	public void setNombreModelo(String nombreModelo) {
		this.nombreModelo = nombreModelo;
	}

	public String getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	public Long getTiempoRespuestaMs() {
		return tiempoRespuestaMs;
	}

	public void setTiempoRespuestaMs(Long tiempoRespuestaMs) {
		this.tiempoRespuestaMs = tiempoRespuestaMs;
	}

	public boolean isEsMejorRespuesta() {
		return esMejorRespuesta;
	}

	public void setEsMejorRespuesta(boolean esMejorRespuesta) {
		this.esMejorRespuesta = esMejorRespuesta;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getUrlArchivo() {
		return urlArchivo;
	}

	public void setUrlArchivo(String urlArchivo) {
		this.urlArchivo = urlArchivo;
	}
}