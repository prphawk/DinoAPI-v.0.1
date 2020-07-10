package br.ufrgs.inf.pet.dinoapi.service.auth.dino;

import br.ufrgs.inf.pet.dinoapi.entity.Auth;
import br.ufrgs.inf.pet.dinoapi.entity.User;
import br.ufrgs.inf.pet.dinoapi.repository.AuthRepository;
import br.ufrgs.inf.pet.dinoapi.service.user.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    AuthRepository authRepository;

    @Autowired
    UserServiceImpl userService;

    private String key = "ie!>[1roh]f!7RmdPpzJ?sAQ(55+#E(RG@LXG*k[CPU4S^35ALLhÇF071[v>p[@t/SX]TD}504T)5|3:iAg2jE/I[yUKN5}N[_iyxç";

    private String secretKey = Base64.getEncoder().encodeToString(key.getBytes());

    private long validityInMilliseconds = 3600000;

    @Override
    public Auth refreshAuth(Auth auth) {
        if (auth != null) {
            final Auth newAuth = generateAuth(auth.getUser());
            authRepository.delete(auth);

            return newAuth;
        }

        return null;
    }

    @Override
    public Auth generateAuth(User user) {
        if (user != null) {
            return createToken(user, new ArrayList<>());
        }

        return null;
    }

    @Override
    public Auth findByAccessToken(String accessToken) {
        if (accessToken.isEmpty()) {
            return null;
        }

        final Optional<Auth> authSearch = authRepository.findByAccessToken(accessToken);

        if (authSearch.isPresent()) {
            return authSearch.get();
        }

        return null;
    }

    @Override
    public Auth getCurrentAuth() {
        final UserDetails userDetails = this.getPrincipal();

        if (userDetails == null) {
            return null;
        }

        final String accessToken = userDetails.getPassword();

        return findByAccessToken(accessToken);
    }

    @Override
    public ResponseEntity<?> logout() {
        authRepository.delete(getCurrentAuth());

        return new ResponseEntity<>("Autenticação removida.", HttpStatus.OK);
    }

    @Override
    public UserDetails getPrincipal() {
        final SecurityContext context =  SecurityContextHolder.getContext();

        if (context == null) {
            return null;
        }

        final Authentication auth = context.getAuthentication();

        if (auth == null) {
            return null;
        }

        return (UserDetails) auth.getPrincipal();
    }

    private Auth createToken(User user, List<String> roles) {
        final Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("roles", roles);
        final Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        final String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        final Auth auth = new Auth(accessToken, validity.getTime());

        auth.setUser(user);

        authRepository.save(auth);

        return auth;
    }
}
