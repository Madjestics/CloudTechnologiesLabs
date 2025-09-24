package com.example.cloudlabs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Entity
@Table(name = "movie", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class Movie {
    @Id
    private Long id;
    private String title;
    private Integer year;
    private LocalTime duration;
    private Double rating;
    private String genre;
    @Column(name = "director")
    private Long directorId;
    @Column(name = "filepath")
    private String filePath;

    public Movie(Long id, String title, Integer year, LocalTime duration, Double rating, String genre, Long directorId) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.duration = duration;
        this.genre = genre;
        this.rating = rating;
        this.directorId = directorId;
    }
}
