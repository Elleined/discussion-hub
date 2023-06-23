package com.forum.application.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_modal_tracker")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ModalTracker {

    @Id
    @Column(name = "user_id")
    private int userId;

    @Column(name = "modal_type")
    @Enumerated(EnumType.STRING)
    private Type type;


    @Column(name = "associated_id_opened")
    private int associatedTypeIdOpened;
}
