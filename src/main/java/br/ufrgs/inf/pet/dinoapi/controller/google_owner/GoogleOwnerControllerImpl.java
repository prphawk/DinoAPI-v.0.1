package br.ufrgs.inf.pet.dinoapi.controller.google_owner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class GoogleOwnerControllerImpl implements GoogleOwnerController {

    @Override
    @GetMapping("google51f841a66886c396.html")
    public ResponseEntity<String> get() {
        return new ResponseEntity<>("google-site-verification: google51f841a66886c396.html", HttpStatus.OK);
    }

}
