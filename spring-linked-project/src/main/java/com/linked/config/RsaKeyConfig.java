package com.linked.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

/**
 * Par de claves RSA con el que el cliente web cifra las contraseñas antes de enviarlas.
 * <p>
 * La clave pública se publica en {@code GET /api/auth/public-key} y la privada la usa
 * {@link com.linked.security.RsaDecryptionService}. Si el archivo {@code app.rsa.key-path}
 * no existe se genera un par nuevo; en cada entorno conviene conservarlo entre reinicios
 * para que la clave pública que ya descargó el cliente siga siendo válida.
 */
@Configuration
public class RsaKeyConfig {

    private final KeyPair keyPair;

    public RsaKeyConfig(@Value("${app.rsa.key-path:rsa-keypair.ser}") String keyPath) throws Exception {
        this.keyPair = loadOrGenerate(Path.of(keyPath));
    }

    @Bean
    public PublicKey rsaPublicKey() {
        return keyPair.getPublic();
    }

    @Bean
    public PrivateKey rsaPrivateKey() {
        return keyPair.getPrivate();
    }

    private static KeyPair loadOrGenerate(Path path) throws Exception {
        if (Files.exists(path)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
                return (KeyPair) in.readObject();
            }
        }
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))) {
            out.writeObject(pair);
        }
        return pair;
    }
}
