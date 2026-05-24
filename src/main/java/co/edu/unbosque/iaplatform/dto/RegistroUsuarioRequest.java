package co.edu.unbosque.iaplatform.dto;

/**
 * DTO para la solicitud de registro de usuario. Contiene los datos necesarios
 * para crear una cuenta nueva.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
public class RegistroUsuarioRequest {

	private String email;
	private String nombre;
	private String apellido;
	private String password;

	public RegistroUsuarioRequest() {
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}