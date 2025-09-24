package com.example.cloudlabs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MovieEvent {
    private Action action;
    private MovieDto movie;
}
