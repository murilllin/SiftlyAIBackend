package co.edu.unbosque.iaplatform.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferir mensajes al frontend. Incluye las respuestas de IA
 * asociadas al mensaje.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
public class MensajeDTO {

	private long id;
	private long conversacionId;
	private String tipo;
	private String contenido;
	private String tipoContenido;
	private LocalDateTime fechaCreacion;
	private List<RespuestaIADTO> respuestasIA;
	private Long mejorRespuestaId;

	public MensajeDTO() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getConversacionId() {
		return conversacionId;
	}

	public void setConversacionId(long conversacionId) {
		this.conversacionId = conversacionId;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public String getTipoContenido() {
		return tipoContenido;
	}

	public void setTipoContenido(String tipoContenido) {
		this.tipoContenido = tipoContenido;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public List<RespuestaIADTO> getRespuestasIA() {
		return respuestasIA;
	}

	public void setRespuestasIA(List<RespuestaIADTO> respuestasIA) {
		this.respuestasIA = respuestasIA;
	}

	public Long getMejorRespuestaId() {
		return mejorRespuestaId;
	}

	public void setMejorRespuestaId(Long mejorRespuestaId) {
		this.mejorRespuestaId = mejorRespuestaId;
	}
}