package br.ufrgs.inf.pet.dinoapi.service.auth.google;

import br.ufrgs.inf.pet.dinoapi.communication.google.GoogleAPICommunicationImpl;
import br.ufrgs.inf.pet.dinoapi.entity.auth.Auth;
import br.ufrgs.inf.pet.dinoapi.entity.auth.GoogleAuth;
import br.ufrgs.inf.pet.dinoapi.entity.user.User;
import br.ufrgs.inf.pet.dinoapi.exception.GoogleClientSecretIOException;
import br.ufrgs.inf.pet.dinoapi.model.auth.AuthResponseModel;
import br.ufrgs.inf.pet.dinoapi.model.auth.google.GoogleAuthRequestModel;
import br.ufrgs.inf.pet.dinoapi.model.auth.google.GoogleRefreshAuthResponseModel;
import br.ufrgs.inf.pet.dinoapi.model.user.UserResponseModel;
import br.ufrgs.inf.pet.dinoapi.repository.auth.GoogleAuthRepository;
import br.ufrgs.inf.pet.dinoapi.service.auth.AuthServiceImpl;
import br.ufrgs.inf.pet.dinoapi.service.user.UserServiceImpl;
import com.google.api.client.googleapis.auth.oauth2.*;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.Optional;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final UserServiceImpl userService;

    private final AuthServiceImpl authService;

    private final GoogleAuthRepository googleAuthRepository;

    private final GoogleAPICommunicationImpl googleAPICommunicationImpl;

    @Autowired
    public GoogleAuthServiceImpl(UserServiceImpl userService, AuthServiceImpl authService, GoogleAuthRepository googleAuthRepository, GoogleAPICommunicationImpl googleAPICommunicationImpl) {
        this.userService = userService;
        this.authService = authService;
        this.googleAuthRepository = googleAuthRepository;
        this.googleAPICommunicationImpl = googleAPICommunicationImpl;
    }

    @Override
    public ResponseEntity<?> googleSignIn(GoogleAuthRequestModel authModel) {
        try {
            final GoogleIdToken idToken = googleAPICommunicationImpl.getGoogleToken(authModel.getIdToken());

            if (idToken != null) {
                final GoogleIdToken.Payload payload = idToken.getPayload();

                final String googleId = payload.getSubject();

                GoogleAuth googleAuth = this.getGoogleAuthByGoogleId(googleId);

                User user; 

                if (googleAuth != null) {
                    user = this.updateUserData(payload);
                } else {
                    final String email = payload.getEmail();
                    final String name = (String) payload.get("name");
                    final String pictureUrl = (String) payload.get("picture");

                    user = userService.create(name, email, pictureUrl);

                    googleAuth = new GoogleAuth(googleId, user);

                    googleAuthRepository.save(googleAuth);
                }

                final Auth auth = authService.generateAuth(user);

                final Claims claims = authService.decodeAccessToken(auth.getAccessToken());

                final UserResponseModel userResponseModel = new UserResponseModel();

                userResponseModel.setEmail(user.getEmail());

                userResponseModel.setName(user.getName());

                userResponseModel.setPictureURL(user.getPictureURL());

                userResponseModel.setVersion(user.getVersion());

                final AuthResponseModel authResponseModel = new AuthResponseModel();

                authResponseModel.setAccessToken(auth.getAccessToken());

                authResponseModel.setExpiresDate(claims.getExpiration().getTime());

                authResponseModel.setUser(userResponseModel);

                return new ResponseEntity<>(authResponseModel, HttpStatus.OK);
            }
        } catch (GoogleClientSecretIOException e) {
            return new ResponseEntity<>("Internal auth error.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (GeneralSecurityException e) {
            return new ResponseEntity<>("Google validate error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Google auth error.", HttpStatus.BAD_REQUEST);
    }

    private GoogleAuth getGoogleAuthByGoogleId(String googleId) {
        final Optional<GoogleAuth> googleAuthSearchResult = googleAuthRepository.findByGoogleId(googleId);

        if (googleAuthSearchResult.isPresent()) {
            return googleAuthSearchResult.get();
        }

        return null;
    }

    private User updateUserData(GoogleIdToken.Payload payload) {
        final String email = payload.getEmail();
        final String name = (String) payload.get("name");
        final String pictureUrl = (String) payload.get("picture");

        return userService.update(name, email, pictureUrl);
    }
}
