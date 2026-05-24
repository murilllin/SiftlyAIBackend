package co.edu.unbosque.iaplatform.controller;

import co.edu.unbosque.iaplatform.dto.LoginRequest;
import co.edu.unbosque.iaplatform.dto.RegistroUsuarioRequest;
import co.edu.unbosque.iaplatform.dto.UsuarioDTO;
import co.edu.unbosque.iaplatform.entity.AuditoriaLog.TipoAccion;
import co.edu.unbosque.iaplatform.entity.CodigoVerificacion;
import co.edu.unbosque.iaplatform.entity.Usuario;
import co.edu.unbosque.iaplatform.repository.CodigoVerificacionRepository;
import co.edu.unbosque.iaplatform.repository.UsuarioRepository;
import co.edu.unbosque.iaplatform.security.JwtUtil;
import co.edu.unbosque.iaplatform.service.AuditoriaService;
import co.edu.unbosque.iaplatform.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.*;

/**
 * Controlador de autenticación y gestión de usuarios.
 * <p>
 * Proporciona endpoints para:
 * <ul>
 *   <li>Registro de usuarios con verificación por email</li>
 *   <li>Login con JWT</li>
 *   <li>Refresh de tokens</li>
 *   <li>Proxy para imágenes y videos (evita CORS)</li>
 *   <li>CRUD de usuarios (solo ADMIN)</li>
 * </ul>
 * </p>
 * 
 * @author Daniel Murillo
 * @version 1.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${api.openrouter.key}")
    private String apiKey;

    @Autowired private RestTemplate restTemplate;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CodigoVerificacionRepository codigoRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private AuditoriaService auditoriaService;

    @Value("${app.verificacion.expiracion-minutos:15}")
    private int minutosExpiracion;

    private final SecureRandom random = new SecureRandom();

    /**
     * Proxy para videos de APIs externas.
     *
     * @param url URL del video a proxy
     * @return Video en formato MP4
     */
    @GetMapping("/public/video")
    public ResponseEntity<byte[]> publicVideo(@RequestParam String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .header("Content-Disposition", "inline")
                .header("Accept-Ranges", "bytes")
                .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Proxy para imágenes de APIs externas.
     *
     * @param url URL de la imagen a proxy
     * @return Imagen en formato PNG o su tipo original
     */
    @GetMapping("/public/image")
    public ResponseEntity<byte[]> publicImage(@RequestParam String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (url.contains("openrouter.ai")) {
                headers.set("Authorization", "Bearer " + apiKey);
            }
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            MediaType contentType = response.getHeaders().getContentType();
            if (contentType == null) contentType = MediaType.IMAGE_PNG;
            return ResponseEntity.ok().contentType(contentType).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Registra un nuevo usuario y envía código de verificación por email.
     *
     * @param req     Datos de registro
     * @param httpReq HttpServletRequest para auditoría
     * @return Mensaje de éxito o error
     */
    @PostMapping("/registro")
    @Transactional
    public ResponseEntity<?> registrar(@RequestBody RegistroUsuarioRequest req,
                                       HttpServletRequest httpReq) {
        if (req.getEmail() == null || req.getPassword() == null ||
            req.getNombre() == null || req.getApellido() == null) {
            return ResponseEntity.badRequest().body(error("Todos los campos son obligatorios"));
        }
        if (usuarioRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(error("El email ya esta registrado"));
        }

        String hashPassword = passwordEncoder.encode(req.getPassword());
        Usuario usuario = new Usuario();
        usuario.setEmail(req.getEmail());
        usuario.setNombre(req.getNombre());
        usuario.setApellido(req.getApellido());
        usuario.setPassword(hashPassword);
        usuario.setRol("USUARIO");
        usuario.setActivo(true);
        usuario.setVerificado(false);
        usuario.setProveedor("LOCAL");
        usuarioRepository.save(usuario);

        String codigo = String.format("%06d", random.nextInt(1_000_000));
        
        codigoRepository.deleteByEmail(req.getEmail());
        
        codigoRepository.save(new CodigoVerificacion(req.getEmail(), codigo, minutosExpiracion));
        
        emailService.enviarCodigoVerificacion(req.getEmail(), codigo, req.getNombre());

        auditoriaService.registrar(usuario, TipoAccion.REGISTRO,
            "Nuevo registro: " + req.getEmail(), getIp(httpReq));

        return ResponseEntity.ok(Map.of(
            "message", "Registro exitoso. Revisa tu correo para verificar la cuenta.",
            "email", req.getEmail()
        ));
    }
    /**
     * Verifica el código enviado al email del usuario.
     *
     * @param req     Mapa con email y codigo
     * @param httpReq HttpServletRequest para auditoría
     * @return Tokens JWT y datos del usuario si es exitoso
     */
    @PostMapping("/verificar-codigo")
    public ResponseEntity<?> verificarCodigo(@RequestBody Map<String, String> req,
                                              HttpServletRequest httpReq) {
        String email  = req.get("email");
        String codigo = req.get("codigo");

        if (email == null || codigo == null) {
            return ResponseEntity.badRequest().body(error("Email y codigo son requeridos"));
        }
        Optional<CodigoVerificacion> codigoOpt = codigoRepository.findByEmailAndUsadoFalse(email);
        if (codigoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(error("No hay un codigo pendiente para este email"));
        }
        CodigoVerificacion cv = codigoOpt.get();
        if (cv.estaExpirado()) {
            return ResponseEntity.badRequest().body(error("El codigo ha expirado. Solicita uno nuevo."));
        }
        if (!cv.getCodigo().equals(codigo)) {
            return ResponseEntity.badRequest().body(error("Codigo incorrecto"));
        }

        cv.setUsado(true);
        codigoRepository.save(cv);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(error("Usuario no encontrado"));
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setVerificado(true);
        usuarioRepository.save(usuario);

        auditoriaService.registrar(usuario, TipoAccion.VERIFICACION_CODIGO,
            "Verificación exitosa: " + email, getIp(httpReq));

        String token        = jwtUtil.generarToken(usuario.getEmail(), usuario.getRol(), usuario.getId());
        String refreshToken = jwtUtil.generarRefreshToken(usuario.getEmail());

        return ResponseEntity.ok(Map.of(
            "token", token,
            "refreshToken", refreshToken,
            "usuario", toDTO(usuario)
        ));
    }

    /**
     * Autentica un usuario y genera tokens JWT.
     *
     * @param req     Credenciales de login
     * @param httpReq HttpServletRequest para auditoría
     * @return Tokens JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req,
                                   HttpServletRequest httpReq) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(req.getEmail());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(error("Credenciales invalidas"));
        }
        Usuario usuario = usuarioOpt.get();

        if (!usuario.isActivo()) {
            auditoriaService.registrarFallo(usuario, TipoAccion.LOGIN,
                "Intento de login con cuenta inactiva: " + req.getEmail(), getIp(httpReq));
            return ResponseEntity.badRequest().body(error("Usuario inactivo"));
        }
        if (!passwordEncoder.matches(req.getPassword(), usuario.getPassword())) {
            auditoriaService.registrarFallo(usuario, TipoAccion.LOGIN,
                "Contraseña incorrecta para: " + req.getEmail(), getIp(httpReq));
            return ResponseEntity.badRequest().body(error("Credenciales invalidas"));
        }
        if (!usuario.isVerificado() && "LOCAL".equals(usuario.getProveedor())) {
            return ResponseEntity.status(403).body(error("Cuenta no verificada. Revisa tu correo."));
        }

        auditoriaService.registrar(usuario, TipoAccion.LOGIN,
            "Login exitoso: " + req.getEmail(), getIp(httpReq));

        String token        = jwtUtil.generarToken(usuario.getEmail(), usuario.getRol(), usuario.getId());
        String refreshToken = jwtUtil.generarRefreshToken(usuario.getEmail());

        return ResponseEntity.ok(Map.of(
            "token", token,
            "refreshToken", refreshToken,
            "usuario", toDTO(usuario)
        ));
    }

    /**
     * Cierra la sesión del usuario (registra el evento en auditoría).
     *
     * @param httpReq HttpServletRequest para obtener IP
     * @param auth    Autenticación actual
     * @return Mensaje de confirmación
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpReq,
                                    org.springframework.security.core.Authentication auth) {
        if (auth != null) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(auth.getName());
            auditoriaService.registrar(
                usuarioOpt.orElse(null),
                TipoAccion.LOGOUT,
                "Logout: " + auth.getName(),
                getIp(httpReq));
        }
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada"));
    }

    /**
     * Renueva el token de acceso usando un refresh token válido.
     *
     * @param req Mapa con refreshToken
     * @return Nuevos tokens JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> req) {
        String refreshToken = req.get("refreshToken");
        if (refreshToken == null || !jwtUtil.esValido(refreshToken) || jwtUtil.estaExpirado(refreshToken)) {
            return ResponseEntity.status(401).body(error("Refresh token invalido o expirado"));
        }
        String email = jwtUtil.extraerEmail(refreshToken);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().isActivo()) {
            return ResponseEntity.status(401).body(error("Usuario no encontrado o inactivo"));
        }
        Usuario usuario     = usuarioOpt.get();
        String nuevoToken   = jwtUtil.generarToken(usuario.getEmail(), usuario.getRol(), usuario.getId());
        String nuevoRefresh = jwtUtil.generarRefreshToken(usuario.getEmail());
        return ResponseEntity.ok(Map.of("token", nuevoToken, "refreshToken", nuevoRefresh));
    }

    /**
     * Reenvía el código de verificación a un email registrado.
     *
     * @param req Mapa con email
     * @return Mensaje de confirmación
     */
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<?> reenviarCodigo(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        if (email == null) return ResponseEntity.badRequest().body(error("Email requerido"));
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) return ResponseEntity.badRequest().body(error("Email no registrado"));
        Usuario usuario = usuarioOpt.get();
        if (usuario.isVerificado()) return ResponseEntity.badRequest().body(error("La cuenta ya esta verificada"));
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        codigoRepository.deleteByEmail(email);
        codigoRepository.save(new CodigoVerificacion(email, codigo, minutosExpiracion));
        emailService.enviarCodigoVerificacion(email, codigo, usuario.getNombre());
        return ResponseEntity.ok(Map.of("message", "Codigo reenviado correctamente"));
    }

    /**
     * Lista todos los usuarios (solo ADMIN).
     *
     * @return Lista de usuarios
     */
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioDTO> response = usuarios.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un usuario por ID (solo ADMIN).
     *
     * @param id ID del usuario
     * @return Usuario encontrado o 404
     */
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable long id) {
        return usuarioRepository.findById(id)
            .map(u -> ResponseEntity.ok((Object) toDTO(u)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza los datos de un usuario (solo ADMIN).
     *
     * @param id  ID del usuario
     * @param req Nuevos datos
     * @return Usuario actualizado
     */
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable long id,
                                               @RequestBody RegistroUsuarioRequest req) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) return ResponseEntity.notFound().build();
        Usuario usuario = usuarioOpt.get();
        usuario.setNombre(req.getNombre());
        usuario.setApellido(req.getApellido());
        usuario.setEmail(req.getEmail());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(toDTO(usuario));
    }

    /**
     * Elimina un usuario del sistema (solo ADMIN).
     *
     * @param id     ID del usuario
     * @param httpReq HttpServletRequest para auditoría
     * @return Confirmación de éxito
     */
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable long id,
                                             HttpServletRequest httpReq) {
        if (!usuarioRepository.existsById(id)) return ResponseEntity.notFound().build();
        Optional<Usuario> u = usuarioRepository.findById(id);
        usuarioRepository.deleteById(id);
        auditoriaService.registrar(null, TipoAccion.ELIMINAR_USUARIO,
            "Usuario eliminado id=" + id + (u.isPresent() ? " email=" + u.get().getEmail() : ""),
            getIp(httpReq));
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Activa o desactiva un usuario (solo ADMIN).
     *
     * @param id     ID del usuario
     * @param req    Mapa con campo "activo"
     * @param httpReq HttpServletRequest para auditoría
     * @return Nuevo estado del usuario
     */
    @PatchMapping("/usuarios/{id}/activar")
    public ResponseEntity<?> toggleActivo(@PathVariable long id,
                                          @RequestBody Map<String, Boolean> req,
                                          HttpServletRequest httpReq) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) return ResponseEntity.notFound().build();
        Usuario usuario = usuarioOpt.get();
        boolean nuevoEstado = req.getOrDefault("activo", true);
        usuario.setActivo(nuevoEstado);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(usuario,
            nuevoEstado ? TipoAccion.ACTIVAR_USUARIO : TipoAccion.DESACTIVAR_USUARIO,
            "Usuario " + (nuevoEstado ? "activado" : "desactivado") + ": " + usuario.getEmail(),
            getIp(httpReq));
        return ResponseEntity.ok(Map.of("success", true, "activo", usuario.isActivo()));
    }

    private UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setNombre(u.getNombre());
        dto.setApellido(u.getApellido());
        dto.setRol(u.getRol());
        dto.setActivo(u.isActivo());
        return dto;
    }

    private Map<String, String> error(String msg) {
        return Map.of("error", msg);
    }

    private String getIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim() : req.getRemoteAddr();
    }
}
