package com.linked.security;

import com.linked.exception.AppExceptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.util.Base64;

/**
 * Descifra las contraseñas que el cliente web envía cifradas con la clave pública RSA
 * (ver {@link com.linked.config.RsaKeyConfig}).
 * <p>
 * Con {@code app.rsa.plaintext-fallback=true} (solo en desarrollo) un valor que no se puede
 * descifrar se toma como texto plano, lo que permite probar la API desde Swagger o Postman.
 */
@Service
public class RsaDecryptionService {

    private final PrivateKey rsaPrivateKey;
    private final boolean plaintextFallback;

    // Web Crypto (RSA-OAEP con SHA-256) usa SHA-256 tanto para el digest como para MGF1.
    // El alias "OAEPWithSHA-256AndMGF1Padding" de Java usa SHA-1 en MGF1, por eso los
    // parámetros se declaran de forma explícita.
    private static final OAEPParameterSpec OAEP_SHA256 = new OAEPParameterSpec(
            "SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);

    public RsaDecryptionService(PrivateKey rsaPrivateKey,
                                @Value("${app.rsa.plaintext-fallback:false}") boolean plaintextFallback) {
        this.rsaPrivateKey = rsaPrivateKey;
        this.plaintextFallback = plaintextFallback;
    }

    /**
     * @param input texto cifrado en Base64
     * @return el texto descifrado
     * @throws AppExceptions.BadRequestException si no se puede descifrar y el fallback está deshabilitado
     */
    public String decrypt(String input) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey, OAEP_SHA256);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(input));
            return new String(decrypted);
        } catch (Exception e) {
            if (plaintextFallback) {
                return input;
            }
            throw new AppExceptions.BadRequestException("No se pudo descifrar la contraseña");
        }
    }
}
