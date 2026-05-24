package co.edu.unbosque.iaplatform.entity;

import co.edu.unbosque.iaplatform.configuration.AesEncryptor;
import jakarta.persistence.*;

/**
 * Entidad que representa un usuario del sistema. El email se almacena
 * encriptado en la base de datos.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Entity
@Table(name = "usuarios54")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Convert(converter = AesEncryptor.class)
	@Column(nullable = false, unique = true, length = 512)
	private String email;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(nullable = false, length = 100)
	private String apellido;

	@Column(nullable = false)
	private String password;

	@Column(name = "rol", nullable = false, length = 20)
	private String rol;

	@Column(nullable = false)
	private boolean activo;

	@Column(nullable = false)
	private boolean verificado;

	@Column(nullable = false, length = 20)
	private String proveedor;

	public Usuario() {
		this.rol = "USUARIO";
		this.activo = true;
		this.verificado = false;
		this.proveedor = "LOCAL";
	}

	/**
	 * Constructor con parámetros básicos.
	 *
	 * @param email    Correo electrónico
	 * @param nombre   Nombre del usuario
	 * @param apellido Apellido del usuario
	 * @param password Contraseña encriptada
	 * @param rol      Rol (ADMIN o USUARIO)
	 * @param activo   Estado activo/inactivo
	 */
	public Usuario(String email, String nombre, String apellido, String password, String rol, boolean activo) {
		this.email = email;
		this.nombre = nombre;
		this.apellido = apellido;
		this.password = password;
		this.rol = rol;
		this.activo = activo;
		this.verificado = false;
		this.proveedor = "LOCAL";
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public boolean isVerificado() {
		return verificado;
	}

	public void setVerificado(boolean verificado) {
		this.verificado = verificado;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}
}