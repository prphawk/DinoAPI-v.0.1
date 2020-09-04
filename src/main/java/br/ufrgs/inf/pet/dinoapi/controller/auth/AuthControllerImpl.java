package br.ufrgs.inf.pet.dinoapi.controller.auth;

import br.ufrgs.inf.pet.dinoapi.model.auth.AuthRefreshRequestModel;
import br.ufrgs.inf.pet.dinoapi.model.auth.google.GoogleAuthRequestModel;
import br.ufrgs.inf.pet.dinoapi.service.auth.AuthServiceImpl;
import br.ufrgs.inf.pet.dinoapi.service.auth.google.GoogleAuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
public class AuthControllerImpl implements AuthController {

    private final GoogleAuthServiceImpl googleAuthService;

    private final AuthServiceImpl authService;

    @Autowired
    public AuthControllerImpl(GoogleAuthServiceImpl googleAuthService, AuthServiceImpl authService) {
        this.googleAuthService = googleAuthService;
        this.authService = authService;
    }

    @Override
    @PostMapping("/public/auth/google/")
    public ResponseEntity<?> googleAuthRequest(@Valid @RequestBody GoogleAuthRequestModel authRequestMode) {
        return googleAuthService.googleSignIn(authRequestMode);
    }

    @Override
    @PutMapping("/public/auth/refresh/")
    public ResponseEntity<?> refreshAuth(@Valid @RequestBody AuthRefreshRequestModel authRefreshRequestModel) {
        return authService.refreshAuth(authRefreshRequestModel);
    }
    
    @GetMapping("/public/teste")
    public ResponseEntity<String> getTest() {
        return new ResponseEntity<>("teste", HttpStatus.OK);
    }

    @Override
    @GetMapping("/auth/google/")
    public ResponseEntity<?> googleRefreshAuth() {
        return googleAuthService.googleRefreshAuth();
    }

    @Override
    @PutMapping("/auth/logout/")
    public ResponseEntity<?> logout() {
        return authService.logout();
    }
}
