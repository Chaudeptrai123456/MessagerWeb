//package com.example.Messenger.Controller;
//
//import com.nimbusds.jose.jwk.JWKSet;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//@RestController
//class JwkSetController {
//    private final JWKSet jwkSet;
//
//    public JwkSetController(JWKSet jwkSet) {
//        this.jwkSet = jwkSet;
//    }
//
//    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
//    public String keys() {
//        // trả object JSON của JWKS
//        return Mono.fromSupplier(() -> jwkSet.toJSONObject().toString());
//    }
//}
