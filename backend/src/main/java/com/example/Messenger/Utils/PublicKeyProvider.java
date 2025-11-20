package com.example.Messenger.Utils;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.PublicKey;

@Component
public class PublicKeyProvider {
//
//    private final PublicKey publicKey;
//
//    public PublicKeyProvider() throws Exception {
//        URL jwksURL = new URL("http://localhost:9999/oauth2/jwks");
//        JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(jwksURL);
//
//        JWK jwk = jwkSource.get(null).get(0); // lấy key đầu tiên từ JWKS
//        this.publicKey = ((RSAKey) jwk).toRSAPublicKey();
//    }
//
//    public PublicKey getPublicKey() {
//        return publicKey;
//    }
}
