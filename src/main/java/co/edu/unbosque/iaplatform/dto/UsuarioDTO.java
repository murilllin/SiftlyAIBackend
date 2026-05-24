package co.edu.unbosque.iaplatform.dto;

/**
 * DTO para transferir datos de usuario al frontend. Excluye información
 * sensible como la contraseña.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
public class UsuarioDTO {

	private long id;
	private String email;
	private String nombre;
	private String apellido;
	private String rol;
	private boolean activo;

	public UsuarioDTO() {
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

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
}