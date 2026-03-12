package com.example.taskmanager.repository;

import com.example.taskmanager.model.Context;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContextRepository extends JpaRepository<Context, Long> {
    // all contexts for a user
    List<Context> findAllByUserId(Long userid);
}
