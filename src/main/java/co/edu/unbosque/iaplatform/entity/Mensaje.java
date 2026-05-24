package co.edu.unbosque.iaplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un mensaje dentro de una conversación. Puede ser de
 * tipo USUARIO o IA, y contener texto, imagen, video o audio.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Entity
@Table(name = "mensajes54")
public class Mensaje {

	public enum TipoMensaje {
		USUARIO, IA
	}

	public enum TipoContenido {
		TEXTO, IMAGEN, VIDEO, AUDIO_TTS
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conversacion_id", nullable = false)
	private Conversacion conversacion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoMensaje tipo;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String contenido;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_contenido", nullable = false)
	private TipoContenido tipoContenido;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "mejor_respuesta_id")
	private Long mejorRespuestaId;

	public Mensaje() {
		this.fechaCreacion = LocalDateTime.now();
		this.tipoContenido = TipoContenido.TEXTO;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Conversacion getConversacion() {
		return conversacion;
	}

	public void setConversacion(Conversacion conversacion) {
		this.conversacion = conversacion;
	}

	public TipoMensaje getTipo() {
		return tipo;
	}

	public void setTipo(TipoMensaje tipo) {
		this.tipo = tipo;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public TipoContenido getTipoContenido() {
		return tipoContenido;
	}

	public void setTipoContenido(TipoContenido tipoContenido) {
		this.tipoContenido = tipoContenido;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Long getMejorRespuestaId() {
		return mejorRespuestaId;
	}

	public void setMejorRespuestaId(Long mejorRespuestaId) {
		this.mejorRespuestaId = mejorRespuestaId;
	}
}