package co.edu.unbosque.iaplatform.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Servicio para carga de usuarios autenticados vía OAuth2 (Google).
 * Extiende DefaultOAuth2UserService para obtener el perfil del usuario.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class OAuth2UsuarioService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest);
    }
}