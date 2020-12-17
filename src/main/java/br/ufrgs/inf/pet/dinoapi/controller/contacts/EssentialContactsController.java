package br.ufrgs.inf.pet.dinoapi.controller.contacts;

import br.ufrgs.inf.pet.dinoapi.model.contacts.*;
import br.ufrgs.inf.pet.dinoapi.model.faq.FaqIdModel;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface EssentialContactsController {

        ResponseEntity<?> saveEssentialContactAll(List<EssentialContactSaveModel> models);

        ResponseEntity<?> setUserTreatmentContacts(FaqIdModel model);
}