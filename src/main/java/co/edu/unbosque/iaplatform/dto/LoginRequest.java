package co.edu.unbosque.iaplatform.dto;

/**
 * DTO para la solicitud de login. Contiene las credenciales del usuario.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
public class LoginRequest {

	private String email;
	private String password;

	public LoginRequest() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}