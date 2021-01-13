package br.ufrgs.inf.pet.dinoapi.service.contact;

import br.ufrgs.inf.pet.dinoapi.communication.google.people.GooglePeopleCommunication;
import br.ufrgs.inf.pet.dinoapi.entity.auth.Auth;
import br.ufrgs.inf.pet.dinoapi.entity.contacts.Contact;
import br.ufrgs.inf.pet.dinoapi.entity.contacts.EssentialContact;
import br.ufrgs.inf.pet.dinoapi.entity.contacts.Phone;
import br.ufrgs.inf.pet.dinoapi.entity.synchronizable.SynchronizableEntity;
import br.ufrgs.inf.pet.dinoapi.entity.treatment.Treatment;
import br.ufrgs.inf.pet.dinoapi.entity.user.User;
import br.ufrgs.inf.pet.dinoapi.exception.synchronizable.AuthNullException;
import br.ufrgs.inf.pet.dinoapi.exception.synchronizable.ConvertModelToEntityException;
import br.ufrgs.inf.pet.dinoapi.model.contacts.ContactDataModel;
import br.ufrgs.inf.pet.dinoapi.model.contacts.EssentialContactDataModel;
import br.ufrgs.inf.pet.dinoapi.model.contacts.GoogleContactDataModel;
import br.ufrgs.inf.pet.dinoapi.model.google.people.GooglePeopleModel;
import br.ufrgs.inf.pet.dinoapi.model.synchronizable.request.SynchronizableDeleteModel;
import br.ufrgs.inf.pet.dinoapi.repository.contact.EssentialContactRepository;
import br.ufrgs.inf.pet.dinoapi.repository.treatment.TreatmentRepository;
import br.ufrgs.inf.pet.dinoapi.service.auth.OAuthServiceImpl;
import br.ufrgs.inf.pet.dinoapi.service.clock.ClockServiceImpl;
import br.ufrgs.inf.pet.dinoapi.service.log_error.LogAPIErrorServiceImpl;
import br.ufrgs.inf.pet.dinoapi.service.synchronizable.SynchronizableServiceImpl;
import br.ufrgs.inf.pet.dinoapi.service.treatment.TreatmentServiceImpl;
import br.ufrgs.inf.pet.dinoapi.service.user.UserServiceImpl;
import br.ufrgs.inf.pet.dinoapi.websocket.enumerable.WebSocketDestinationsEnum;
import br.ufrgs.inf.pet.dinoapi.websocket.service.topic.SynchronizableTopicMessageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EssentialContactServiceImpl extends
        SynchronizableServiceImpl<EssentialContact, Long, Integer, EssentialContactDataModel, EssentialContactRepository> {

    private final TreatmentRepository treatmentRepository;
    private final TreatmentServiceImpl treatmentService;
    private final UserServiceImpl userService;
    private final ContactServiceImpl contactService;
    private final PhoneServiceImpl phoneService;
    private final GooglePeopleCommunication googlePeopleCommunication;
    private final GoogleContactServiceImpl googleContactService;

    @Autowired
    public EssentialContactServiceImpl(TreatmentRepository treatmentRepository, EssentialContactRepository repository, PhoneServiceImpl phoneService,
                                       OAuthServiceImpl authService, ClockServiceImpl clock, UserServiceImpl userService, TreatmentServiceImpl treatmentService,
                                       ContactServiceImpl contactService, LogAPIErrorServiceImpl logAPIErrorService, GooglePeopleCommunication googlePeopleCommunication,
                                       SynchronizableTopicMessageServiceImpl<Long, Integer, EssentialContactDataModel> synchronizableTopicMessageService,
                                       GoogleContactServiceImpl googleContactService) {
        super(repository, authService, clock, synchronizableTopicMessageService, logAPIErrorService);
        this.treatmentRepository = treatmentRepository;
        this.userService = userService;
        this.contactService = contactService;
        this.treatmentService = treatmentService;
        this.phoneService = phoneService;
        this.googlePeopleCommunication = googlePeopleCommunication;
        this.googleContactService = googleContactService;
    }

    @Override
    public EssentialContactDataModel convertEntityToModel(EssentialContact entity) {
        final EssentialContactDataModel model = new EssentialContactDataModel();
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setColor(entity.getColor());
        if(entity.getTreatments() != null) {
            model.setTreatmentIds(entity.getTreatments().stream()
                    .map(SynchronizableEntity::getId).collect(Collectors.toList()));
        }
        return model;
    }

    @Override
    public EssentialContact convertModelToEntity(EssentialContactDataModel model, Auth auth) throws ConvertModelToEntityException, AuthNullException {
        final EssentialContact entity = new EssentialContact();

        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setColor(model.getColor());
        searchTreatments(entity, model.getTreatmentIds());

        return entity;
    }

    @Override
    public void updateEntity(EssentialContact entity, EssentialContactDataModel model, Auth auth) throws ConvertModelToEntityException, AuthNullException {
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setColor(model.getColor());
        searchTreatments(entity, model.getTreatmentIds());
    }

    @Override
    public Optional<EssentialContact> getEntityByIdAndUserAuth(Long id, Auth auth) throws AuthNullException {
        return this.repository.findById(id);
    }

    @Override
    public List<EssentialContact> getEntitiesThatUserCanRead(Auth auth) throws AuthNullException {
        return this.repository.findAll();
    }

    @Override
    public List<EssentialContact> getEntitiesByIdsAndUserAuth(List<Long> ids, Auth auth) throws AuthNullException {
        return (List<EssentialContact>) this.repository.findAllById(ids);
    }

    @Override
    public List<EssentialContact> getEntitiesThatUserCanReadExcludingIds(Auth auth, List<Long> ids) throws AuthNullException {
        return (List<EssentialContact>) this.repository.findAllById(ids);
    }

    @Override
    public WebSocketDestinationsEnum getWebSocketDestination() {
        return WebSocketDestinationsEnum.ESSENTIAL_CONTACT;
    }

    @Override
    protected void onDataCreated(EssentialContactDataModel model) throws AuthNullException, ConvertModelToEntityException {
        final List<Treatment> treatments = treatmentService.getEntitiesByIds(model.getTreatmentIds());

        List<User> users;
        if (treatments.size() > 0) {
            users = userService.findUserBySaveEssentialContactsAndTreatments(treatments);
        } else {
            users = userService.findUserBySaveEssentialContacts();
        }

        for (User user : users) {
            if(user.getUserAppSettings().getIncludeEssentialContact()) {
                final ContactDataModel contactDataModel = new ContactDataModel();
                contactDataModel.setEssentialContactId(model.getId());
                contactDataModel.setColor(model.getColor());
                contactDataModel.setDescription(model.getDescription());
                contactDataModel.setName(model.getName());
                contactDataModel.setLastUpdate(clock.getUTCZonedDateTime());

                final ContactDataModel resultDataModel = contactService.saveByUser(contactDataModel, user);
                final GooglePeopleModel googlePeopleModel = googlePeopleCommunication.createContact(user, resultDataModel);

                if (googlePeopleModel != null) {
                    final GoogleContactDataModel googleContactDataModel = new GoogleContactDataModel();
                    googleContactDataModel.setContactId(contactDataModel.getId());
                    googleContactDataModel.setLastUpdate(this.clock.getUTCZonedDateTime());
                    googleContactDataModel.setResourceName(googlePeopleModel.getResourceName());

                    googleContactService.saveByUser(googleContactDataModel, user);
                }
            }
        }
    }

    @Override
    protected void onDataUpdated(EssentialContactDataModel model, EssentialContact entity) throws AuthNullException, ConvertModelToEntityException {
       final List<Contact> contacts = contactService.findAllByEssentialContactId(model.getId());
       for (Contact contact : contacts) {
           final ContactDataModel contactDataModel = new ContactDataModel();
           contactDataModel.setEssentialContactId(model.getId());
           contactDataModel.setColor(model.getColor());
           contactDataModel.setDescription(model.getDescription());
           contactDataModel.setName(model.getName());
           contactDataModel.setLastUpdate(clock.getUTCZonedDateTime());
           contactDataModel.setId(contact.getId());

           contactService.saveByUser(contactDataModel, contact.getUser());
        }
    }

    @Override
    protected void onDataDeleted(EssentialContact entity) throws AuthNullException {
        final List<Contact> contacts = contactService.findAllByEssentialContactId(entity.getId());

        for (Contact contact : contacts) {
            final List<Phone> phones = phoneService.findAllByContactId(contact.getId());

            final List<SynchronizableDeleteModel<Long>> phoneModels = phones.stream().map(phone -> {
                final SynchronizableDeleteModel<Long> model = new SynchronizableDeleteModel<>();
                model.setLastUpdate(clock.getUTCZonedDateTime());
                model.setId(phone.getId());

                return model;
            }).collect(Collectors.toList());

            phoneService.deleteAllByUser(phoneModels, contact.getUser());

            final SynchronizableDeleteModel<Long> model = new SynchronizableDeleteModel<>();
            model.setLastUpdate(clock.getUTCZonedDateTime());
            model.setId(contact.getId());

            contactService.deleteByUser(model, contact.getUser());
        }
    }

    private void searchTreatments(EssentialContact entity, List<Long> treatmentIds) {
        if(treatmentIds != null && treatmentIds.size() != 0) {
            final List<Treatment> treatmentSearch = treatmentRepository.findAllByIds(treatmentIds);
            entity.setTreatments(treatmentSearch);
        }
    }
}
