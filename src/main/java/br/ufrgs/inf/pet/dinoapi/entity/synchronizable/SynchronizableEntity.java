package br.ufrgs.inf.pet.dinoapi.entity.synchronizable;

import br.ufrgs.inf.pet.dinoapi.model.synchronizable.SynchronizableModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.AUTO;

@Entity
public abstract class SynchronizableEntity<T extends SynchronizableEntity> {
    @Id
    @GeneratedValue(strategy = AUTO)
    @Column(name = "id", nullable = false)
    protected Long id;

    @Column(name = "last_update", nullable = false)
    protected LocalDateTime lastUpdate;

    public SynchronizableEntity() {
        this.lastUpdate = LocalDateTime.now();
    }

    public LocalDateTime getLastUpdate() {
        return this.lastUpdate;
    }

    public Long getId() {
        return this.id;
    }

    public boolean isMoreUpdated(SynchronizableModel<T> model) {
        final LocalDateTime thisLastUpdate = this.getLastUpdate();
        if (thisLastUpdate != null) {
            final LocalDateTime otherLastUpdate = model.getLastUpdate();
            if (otherLastUpdate != null) {
                return this.lastUpdate.compareTo(model.getLastUpdate()) > 0;
            }

            return true;
        }

        return false;
    }
}
