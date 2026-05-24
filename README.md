# SiftlyAI - Backend Spring Boot

## Cambios en esta versión

### Texto (5 modelos)
- ✅ Groq (Llama 3.3) - Gratis
- ✅ Gemini (2.5 Flash) - Gratis con límite diario
- ✅ Cohere (Command R+) - API Key
- ✅ Mistral (Small) - API Key
- ✅ NVIDIA (Llama 3.3 70B + Nemotron 3) - API Key
- ❌ ~~OpenAI (GPT-4o-mini)~~ - Eliminado

### Imagen (3 opciones)
- ✅ Pollinations - Gratis
- ✅ OpenRouter xAI Grok Imagine - $0.05/imagen
- ✅ OpenRouter Recraft v4.1 - $0.04/imagen
- ❌ ~~Leonardo~~ - Eliminado
- ❌ ~~Replicate (FLUX + SD3.5)~~ - Eliminado
- ❌ ~~Gemini Imagen~~ - Eliminado

### Video (3 opciones)
- ✅ OpenRouter Wan 2.6 - $0.04/segundo
- ✅ OpenRouter Veo 3.1 Lite - $0.05/segundo
- ✅ OpenRouter xAI Grok Imagine Video - $0.05/segundo
- ❌ ~~Kling~~ - Eliminado
- ❌ ~~Hailuo~~ - Eliminado
- ❌ ~~Gemini Video (Veo directo)~~ - Eliminado
- ❌ ~~HuggingFace Video~~ - Eliminado

### Audio
- ✅ Groq TTS (Orpheus v1) - Gratis
- ❌ ~~HuggingFace Audio~~ - Eliminado
- ❌ ~~Groq TTS anterior (playai)~~ - Reemplazado

## Configuración rápida

1. **Clonar/reemplazar** este proyecto en tu workspace
2. **Configurar `application.properties`**:
   - `api.openrouter.key` - Tu key de OpenRouter (ya recargada con $5)
   - `api.groq.key` - Tu key de Groq
   - `api.gemini.key` - Tu key de Gemini AI Studio
   - `api.cohere.key` - Tu key de Cohere
   - `api.mistral.key` - Tu key de Mistral
   - `api.nvidia.nim.key` - Tu key de NVIDIA
   - Base de datos MySQL (o usar H2 para pruebas)
   - Configuración de email (Gmail SMTP)

3. **Compilar y ejecutar**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## Endpoints principales

### Auth
- `POST /auth/registro` - Registrar usuario
- `POST /auth/login` - Login
- `POST /auth/verificar-codigo` - Verificar email
- `POST /auth/refresh` - Refresh token
- `GET /auth/oauth2/**` - Google OAuth2

### Conversaciones
- `POST /conversaciones` - Crear conversación
- `GET /conversaciones/usuario/{id}` - Listar por usuario
- `GET /conversaciones/{id}` - Ver conversación con mensajes
- `POST /conversaciones/{id}/mensajes` - Enviar mensaje (TEXTO/IMAGEN/VIDEO)
- `POST /conversaciones/mensajes/{id}/mejor-respuesta` - Seleccionar mejor respuesta

### Media (pruebas directas)
- `POST /media/tts` - Texto a voz (Groq Orpheus)
- `POST /media/video/wan` - Video Wan 2.6
- `POST /media/video/veo-lite` - Video Veo 3.1 Lite
- `POST /media/video/xai` - Video xAI Grok
- `POST /media/imagen/xai` - Imagen xAI Grok
- `POST /media/imagen/recraft` - Imagen Recraft

### Admin
- `GET /admin/usuarios` - Listar usuarios (requiere ADMIN)
- `GET /admin/stats` - Estadísticas
- `PATCH /admin/usuarios/{id}/rol` - Cambiar rol

## Costos estimados con $5 en OpenRouter

| Servicio | Modelo | Precio | Cantidad estimada |
|----------|--------|--------|-------------------|
| Imagen | xAI Grok | $0.05/img | ~100 imágenes |
| Imagen | Recraft | $0.04/img | ~125 imágenes |
| Video | Wan 2.6 | $0.04/s | ~125 segundos |
| Video | Veo 3.1 Lite | $0.05/s | ~100 segundos |
| Video | xAI Grok | $0.05/s | ~100 segundos |

## Notas importantes

- **Pollinations** genera URLs directas, pero requieren abrirse en navegador para cargar la imagen (hotlinking)
- **OpenRouter** usa polling para video: POST → esperar → GET cada 5s hasta `status: completed`
- **Groq TTS Orpheus** devuelve audio WAV de alta calidad con controles de dirección vocal (`[cheerful]`, `[sad]`, etc.)
- **Audio en árabe** disponible vía `/media/tts-arabic` con `canopylabs/orpheus-arabic-saudi`

## Troubleshooting

### Error "No provider for JWT"
Verificar que `jwt.secret` tenga al menos 32 caracteres.

### Error OpenRouter "insufficient credits"
Verificar saldo en https://openrouter.ai/credits

### Error Groq "rate limit"
Groq tiene límites por minuto en el tier gratuito. Esperar unos segundos.

### Video no genera
Algunos modelos de video pueden tardar 30-60 segundos. El polling espera hasta 2.5 minutos.
