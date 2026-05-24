package co.edu.unbosque.iaplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que almacena las respuestas generadas por los diferentes modelos de
 * IA. Cada mensaje puede tener múltiples respuestas de distintos modelos.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Entity
@Table(name = "respuestas_ia54")
public class RespuestaIA {

	public enum ModeloIA {
		GROQ_LLAMA3, COHERE_COMMAND, MISTRAL_SMALL, GEMINI, NVIDIA_LLAMA_3_3_70B, NVIDIA_NEMOTRON_3_SUPER,

		POLLINATIONS_IMG, OPENROUTER_XAI_GROK_IMG, OPENROUTER_RECRAFT_IMG,

		OPENROUTER_WAN_VIDEO, OPENROUTER_VEO_LITE_VIDEO, OPENROUTER_XAI_GROK_VIDEO,

		GROQ_TTS_ORPHEUS
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mensaje_id", nullable = false)
	private Mensaje mensaje;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ModeloIA modeloIA;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String respuesta;

	@Column(name = "tiempo_respuesta_ms")
	private Long tiempoRespuestaMs;

	@Column(name = "es_mejor_respuesta")
	private boolean esMejorRespuesta;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "url_archivo", columnDefinition = "LONGTEXT")
	private String urlArchivo;

	public RespuestaIA() {
		this.fechaCreacion = LocalDateTime.now();
		this.esMejorRespuesta = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Mensaje getMensaje() {
		return mensaje;
	}

	public void setMensaje(Mensaje mensaje) {
		this.mensaje = mensaje;
	}

	public ModeloIA getModeloIA() {
		return modeloIA;
	}

	public void setModeloIA(ModeloIA modeloIA) {
		this.modeloIA = modeloIA;
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
