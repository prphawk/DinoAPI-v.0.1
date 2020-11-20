package br.ufrgs.inf.pet.dinoapi.communication.google;

import br.ufrgs.inf.pet.dinoapi.config.GoogleOAuth2Config;
import br.ufrgs.inf.pet.dinoapi.enumerable.GoogleScopesEnum;
import br.ufrgs.inf.pet.dinoapi.exception.GoogleClientSecretIOException;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;

@Component
public class GoogleAPICommunicationImpl implements GoogleAPICommunication {
    private GoogleOAuth2Config googleOAuth2Config;

    @Autowired
    public GoogleAPICommunicationImpl(GoogleOAuth2Config googleOAuth2Config) {
        this.googleOAuth2Config = googleOAuth2Config;
    }

    public GoogleIdToken getGoogleToken(String idToken) throws GeneralSecurityException, GoogleClientSecretIOException {
        try {
            final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                    .Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleOAuth2Config.getClientid()))
                    .build();

            return verifier.verify(idToken);
        } catch (IOException ex) {
            throw new GoogleClientSecretIOException("Erro ao ler arquivos de configuração do servidor.");
        }
    }

    public GoogleTokenResponse refreshAccessToken(String refreshToken) {
        final ArrayList<String> scopes = new ArrayList<>();

        scopes.add(GoogleScopesEnum.CALENDAR.getScope());
        scopes.add(GoogleScopesEnum.PROFILE.getScope());

        GoogleTokenResponse tokenResponse = null;
        try {
            final GoogleClientSecrets secrets = this.getClientSecrets();

            tokenResponse = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    refreshToken,
                    secrets.getDetails().getClientId(),
                    secrets.getDetails().getClientSecret())
                    .setScopes(scopes)
                    .setGrantType("refresh_token")
                    .execute();

            return tokenResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private GoogleClientSecrets getClientSecrets() throws IOException {
        final String googleSecret = "{\"web\":{\"client_id\":\"398811150587-720e3bk1uvvij6t1a59d8220f620hj6d.apps.googleusercontent.com\",\"project_id\":\"dinoapp-285513\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"2n8Y_xTxfb_jPfJM6mAPZg_i\",\"javascript_origins\":[\"http://localhost:3000\"]}}";
        return GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new StringReader(googleSecret));
    }
}
