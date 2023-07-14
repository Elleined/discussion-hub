package com.forum.application.service;

import com.forum.application.model.ModalTracker;
import com.forum.application.model.Type;
import com.forum.application.repository.ModalTrackerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ModalTrackerService {

    private final ModalTrackerRepository modalTrackerRepository;

    ModalTracker saveTrackerOfUserById(int receiverId, int associateTypeIdOpened, String type) {
        ModalTracker modalTracker = ModalTracker.builder()
                .receiverId(receiverId)
                .associatedTypeIdOpened(associateTypeIdOpened)
                .type(Type.valueOf(type))
                .build();

        var saveModalTracker = modalTrackerRepository.save(modalTracker);
        log.debug("Saving modal tracker for the receiver with id of {} and Type of {} with associated id of {} success!", receiverId, type, associateTypeIdOpened);
        return saveModalTracker;
    }

    ModalTracker getTrackerOfUserById(int userId) {
        return modalTrackerRepository.findById(userId).orElse(null);
    }

    void deleteTrackerOfUserById(int userId, Type type) {
        ModalTracker modalTracker = getTrackerOfUserById(userId);
        if (modalTracker.getType() == type) {
            modalTrackerRepository.deleteById(userId);
            log.debug("Deleting modal tracker for receiver with id of {} success!", userId);
        }
    }

    boolean isModalOpen(int userId, int associatedTypeId, Type type) {
        ModalTracker modalTracker = this.getTrackerOfUserById(userId);
        if (modalTracker == null) return false;
        return modalTracker.getType() == type &&
                modalTracker.getAssociatedTypeIdOpened() == associatedTypeId &&
                modalTracker.getReceiverId() == userId;
    }

    public void deleteAll() {
        modalTrackerRepository.deleteAll();
        log.debug("Deleting all the record in modal tracked success!");
    }
}
