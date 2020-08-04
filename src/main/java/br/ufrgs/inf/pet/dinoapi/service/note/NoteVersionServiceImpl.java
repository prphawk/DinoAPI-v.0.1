package br.ufrgs.inf.pet.dinoapi.service.note;

import br.ufrgs.inf.pet.dinoapi.entity.NoteVersion;
import br.ufrgs.inf.pet.dinoapi.entity.User;
import br.ufrgs.inf.pet.dinoapi.repository.NoteVersionRepository;
import br.ufrgs.inf.pet.dinoapi.service.auth.AuthServiceImpl;
import br.ufrgs.inf.pet.dinoapi.websocket.service.note.NoteWebSocketServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class NoteVersionServiceImpl implements NoteVersionService {

    private final AuthServiceImpl authService;

    private final NoteVersionRepository noteVersionRepository;

    private final NoteWebSocketServiceImpl noteWebSocketService;

    @Autowired
    public NoteVersionServiceImpl(AuthServiceImpl authService, NoteVersionRepository noteVersionRepository, NoteWebSocketServiceImpl noteWebSocketService) {
        this.authService = authService;
        this.noteVersionRepository = noteVersionRepository;
        this.noteWebSocketService = noteWebSocketService;
    }

    @Override
    public ResponseEntity<Long> getVersion() {
        final NoteVersion noteVersion = this.getNoteVersion();

        return new ResponseEntity<>(noteVersion.getVersion(), HttpStatus.OK);
    }

    @Override
    public Long updateVersion() {
        NoteVersion noteVersion = this.getNoteVersion();

        if (noteVersion.getVersion() != noteVersion.DEFAULT_VERSION) {
            noteVersion.updateVersion();
            noteVersion.setLastUpdate(new Date());
            noteVersion = noteVersionRepository.save(noteVersion);

            noteWebSocketService.sendUpdateMessage(noteVersion.getVersion());
        }

        return noteVersion.getVersion();
    }

    private NoteVersion getNoteVersion() {
        final User user = authService.getCurrentAuth().getUser();

        NoteVersion noteVersion = user.getNoteVersion();

        if (noteVersion == null) {
            noteVersion = new NoteVersion(user);

            noteVersion = noteVersionRepository.save(noteVersion);
        }

        return noteVersion;
    }
}
