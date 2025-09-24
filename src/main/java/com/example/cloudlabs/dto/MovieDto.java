package com.example.cloudlabs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private Long id;
    private String title;
    private Integer year;
    private LocalTime duration;
    private Double rating;
    private String genre;
    private Long directorId;
}
