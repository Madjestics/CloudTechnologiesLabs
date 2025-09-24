package com.example.cloudlabs.service;


import com.example.cloudlabs.dto.MovieEvent;
import com.example.cloudlabs.dto.WatchEvent;

public interface MessageService {
    void sendMovieEvent(MovieEvent movieInfo);

    void sendWatchEvent(WatchEvent watchInfo);
}
