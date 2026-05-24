package co.edu.unbosque.iaplatform.configuration;

import co.edu.unbosque.iaplatform.entity.Usuario;
import co.edu.unbosque.iaplatform.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos de la aplicación.
 * Crea usuarios semilla (admin y usuario demo) si no existen en BD.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email:admin@siftly.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:ChangeMe123!}")
    private String adminPassword;

    @Value("${app.seed.admin.nombre:Admin}")
    private String adminNombre;

    @Value("${app.seed.admin.apellido:Siftly}")
    private String adminApellido;

    @Value("${app.seed.user.email:user@siftly.local}")
    private String userEmail;

    @Value("${app.seed.user.password:ChangeMe123!}")
    private String userPassword;

    @Value("${app.seed.user.nombre:Demo}")
    private String userNombre;

    @Value("${app.seed.user.apellido:User}")
    private String userApellido;

    @Override
    public void run(ApplicationArguments args) {
        crearSiNoExiste(adminEmail, adminNombre, adminApellido, adminPassword, "ADMIN");
        crearSiNoExiste(userEmail, userNombre, userApellido, userPassword, "USUARIO");
    }

    private void crearSiNoExiste(String email, String nombre, String apellido,
                                  String pass, String rol) {
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario u = new Usuario();
            u.setEmail(email);
            u.setNombre(nombre);
            u.setApellido(apellido);
            u.setPassword(passwordEncoder.encode(pass));
            u.setRol(rol);
            u.setActivo(true);
            u.setVerificado(true);
            u.setProveedor("LOCAL");
            usuarioRepository.save(u);
        }
    }
}