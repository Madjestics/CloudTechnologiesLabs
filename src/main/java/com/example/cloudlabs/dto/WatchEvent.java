package com.example.cloudlabs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WatchEvent {
    private MovieDto movie;
    private Long userId;
}