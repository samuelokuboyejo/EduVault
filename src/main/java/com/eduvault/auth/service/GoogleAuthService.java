package com.eduvault.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {
    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(@Value("${GOOGLE_CLIENT_ID}") String googleClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        ).setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleIdToken.Payload verifyToken(String idTokenString) throws Exception {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        }
        throw new IllegalArgumentException("Invalid Google ID token");
    }
}

