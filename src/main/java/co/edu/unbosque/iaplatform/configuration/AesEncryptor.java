package co.edu.unbosque.iaplatform.configuration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Convertidor JPA para encriptar/desencriptar campos sensibles usando AES-256.
 * Se utiliza automáticamente con @Convert(converter = AesEncryptor.class).
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Converter
@Component
public class AesEncryptor implements AttributeConverter<String, String> {

    private static final String ALG = "AES/ECB/PKCS5Padding";
    private String secret;

    @Value("${app.encryption.secret:SiftlyEncKey2026}")
    public void setSecret(String s) {
        this.secret = s.length() >= 16 ? s.substring(0, 16) : String.format("%-16s", s).replace(' ', '0');
    }

    @Override
    public String convertToDatabaseColumn(String attr) {
        if (attr == null) return null;
        try {
            Cipher c = Cipher.getInstance(ALG);
            c.init(Cipher.ENCRYPT_MODE, key());
            return Base64.getEncoder().encodeToString(c.doFinal(attr.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error cifrando valor", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String db) {
        if (db == null) return null;
        try {
            Cipher c = Cipher.getInstance(ALG);
            c.init(Cipher.DECRYPT_MODE, key());
            return new String(c.doFinal(Base64.getDecoder().decode(db)));
        } catch (Exception e) {
            throw new RuntimeException("Error descifrando valor (¿clave AES incorrecta?)", e);
        }
    }

    private SecretKeySpec key() {
        if (secret == null) {
            throw new IllegalStateException("AesEncryptor: secret no inicializado");
        }
        return new SecretKeySpec(secret.getBytes(), "AES");
    }
}
