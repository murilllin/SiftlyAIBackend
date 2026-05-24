package co.edu.unbosque.iaplatform.configuration;

import co.edu.unbosque.iaplatform.entity.AuditoriaLog.TipoAccion;
import co.edu.unbosque.iaplatform.entity.Usuario;
import co.edu.unbosque.iaplatform.repository.UsuarioRepository;
import co.edu.unbosque.iaplatform.security.JwtUtil;
import co.edu.unbosque.iaplatform.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Manejador de éxito para autenticación OAuth2 (Google).
 * Extrae datos del perfil, crea o busca el usuario, genera tokens JWT y redirige.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AuditoriaService auditoriaService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("=== OAuth2SuccessHandler INICIADO ===");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String nombre = oAuth2User.getAttribute("given_name");
        String apellido = oAuth2User.getAttribute("family_name");

        if (email == null) {
            log.error("ERROR: email es null en los atributos de Google");
            response.sendRedirect(frontendUrl + "/login?error=no_email");
            return;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        Usuario usuario;

        if (usuarioOpt.isPresent()) {
            usuario = usuarioOpt.get();
            log.info("Usuario existente encontrado: id={}", usuario.getId());
        } else {
            log.info("Usuario nuevo, creando registro...");
            usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setNombre(nombre != null ? nombre : "Usuario");
            usuario.setApellido(apellido != null ? apellido : "");
            usuario.setPassword("OAUTH2_" + java.util.UUID.randomUUID());
            usuario.setRol("USUARIO");
            usuario.setActivo(true);
            usuario.setVerificado(true);
            usuario.setProveedor("GOOGLE");
            usuario = usuarioRepository.save(usuario);
            log.info("Usuario creado con id={}", usuario.getId());
        }

        String token = jwtUtil.generarToken(usuario.getEmail(), usuario.getRol(), usuario.getId());
        String refreshToken = jwtUtil.generarRefreshToken(usuario.getEmail());

        auditoriaService.registrar(usuario, TipoAccion.OAUTH2_LOGIN,
            "Login OAuth2 (Google): " + usuario.getEmail(), getIp(request));

        String redirectUrl = frontendUrl + "/oauth2/callback"
                + "?token=" + token
                + "&refreshToken=" + refreshToken
                + "&nombre=" + java.net.URLEncoder.encode(usuario.getNombre(), "UTF-8")
                + "&apellido=" + java.net.URLEncoder.encode(usuario.getApellido() != null ? usuario.getApellido() : "", "UTF-8")
                + "&email=" + java.net.URLEncoder.encode(usuario.getEmail(), "UTF-8")
                + "&rol=" + usuario.getRol()
                + "&id=" + usuario.getId();

        log.info("=== OAuth2SuccessHandler FIN ===");
        response.sendRedirect(redirectUrl);
    }

    private String getIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim() : req.getRemoteAddr();
    }
}
