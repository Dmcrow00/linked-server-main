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

@Service
public class RsaDecryptionService {

    private final PrivateKey rsaPrivateKey;
    private final boolean plaintextFallback;

    // Web Crypto RSA-OAEP with SHA-256 uses SHA-256 for both digest and MGF1.
    // Java's "OAEPWithSHA-256AndMGF1Padding" shorthand uses SHA-1 for MGF1 by default,
    // so we must specify the parameters explicitly.
    private static final OAEPParameterSpec OAEP_SHA256 = new OAEPParameterSpec(
            "SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);

    public RsaDecryptionService(PrivateKey rsaPrivateKey,
                                @Value("${app.rsa.plaintext-fallback:false}") boolean plaintextFallback) {
        this.rsaPrivateKey = rsaPrivateKey;
        this.plaintextFallback = plaintextFallback;
    }

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
