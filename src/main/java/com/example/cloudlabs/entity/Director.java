package com.example.cloudlabs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "director", schema = "public")
public class Director {
    @Id
    @Column(name="id")
    private Long id;

    @Column(name="fio")
    private String fio;
}
