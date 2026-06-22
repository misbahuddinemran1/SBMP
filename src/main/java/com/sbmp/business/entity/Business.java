package com.sbmp.business.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String businessName;

    private String businessType;

    @Column(columnDefinition = "TEXT")
    private String address;

    private Boolean status = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}