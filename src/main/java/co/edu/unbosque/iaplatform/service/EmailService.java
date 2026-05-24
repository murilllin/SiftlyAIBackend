package co.edu.unbosque.iaplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;

/**
 * Servicio para envío de correos electrónicos.
 * Envía códigos de verificación para registro de usuarios de forma asíncrona.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void enviarCodigoVerificacion(String destinatario, String codigo, String nombre) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(fromEmail, "SiftlyAI");
            helper.setTo(destinatario);
            helper.setSubject("Verificacion de cuenta - SiftlyAI");

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                      <meta charset="UTF-8">
                      <style>
                        body { font-family: 'Segoe UI', sans-serif; background: #0B0E14; color: #E0E0E0; margin: 0; padding: 0; }
                        .container { max-width: 480px; margin: 40px auto; background: #11151C; border-radius: 12px; overflow: hidden; border: 1px solid #1A1D23; }
                        .header { background: linear-gradient(135deg, #003366 0%%, #00C8C8 100%%); padding: 32px; text-align: center; }
                        .header h1 { margin: 0; color: #fff; font-size: 28px; letter-spacing: 1px; }
                        .body { padding: 32px; }
                        .body p { color: #9BA3AF; line-height: 1.6; margin: 0 0 20px; }
                        .code-box { background: #1A1D23; border: 2px solid #00C8C8; border-radius: 10px; padding: 20px; text-align: center; margin: 24px 0; }
                        .code { font-size: 38px; font-weight: 700; letter-spacing: 10px; color: #00C8C8; font-family: monospace; }
                        .expire { color: #6B7280; font-size: 13px; margin-top: 8px; }
                        .footer { padding: 16px 32px; border-top: 1px solid #1A1D23; color: #6B7280; font-size: 12px; text-align: center; }
                      </style>
                    </head>
                    <body>
                      <div class="container">
                        <div class="header">
                          <h1>Siftly<span style="color:#00C8C8">AI</span></h1>
                        </div>
                        <div class="body">
                          <p>Hola <strong style="color:#E0E0E0">%s</strong>,</p>
                          <p>Gracias por registrarte en SiftlyAI. Usa el siguiente codigo para verificar tu cuenta:</p>
                          <div class="code-box">
                            <div class="code">%s</div>
                            <div class="expire">Este codigo expira en 15 minutos</div>
                          </div>
                          <p>Si no creaste esta cuenta, puedes ignorar este correo sin problema.</p>
                        </div>
                        <div class="footer">
                          SiftlyAI &mdash; Plataforma Multi-IA
                        </div>
                      </div>
                    </body>
                    </html>
                    """.formatted(nombre, codigo);

            helper.setText(html, true);
            mailSender.send(msg);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Error enviando correo de verificacion: " + e.getMessage(), e);
        }
    }
}
