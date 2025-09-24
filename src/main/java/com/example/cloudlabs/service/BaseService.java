package com.example.cloudlabs.service;

import java.util.List;

public interface BaseService<T> {

    List<T> findAll();
    T findById(Long id);
    T add(T entity);
    T update(T entity, Long id);
    void delete(Long id);
}
