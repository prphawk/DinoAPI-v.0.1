package br.ufrgs.inf.pet.dinoapi.controller.faq;

import br.ufrgs.inf.pet.dinoapi.controller.synchronizable.SynchronizableControllerImpl;
import br.ufrgs.inf.pet.dinoapi.entity.faq.FaqItem;
import br.ufrgs.inf.pet.dinoapi.model.faq.FaqItemDataModel;
import br.ufrgs.inf.pet.dinoapi.repository.faq.FaqItemRepository;
import br.ufrgs.inf.pet.dinoapi.service.faq.FaqItemServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/faq_item/")
public class FaqItemControllerImpl extends SynchronizableControllerImpl<
        FaqItem, Long, FaqItemDataModel, FaqItemRepository, FaqItemServiceImpl> {

    @Autowired
    protected FaqItemControllerImpl(FaqItemServiceImpl service) {
        super(service);
    }
}
