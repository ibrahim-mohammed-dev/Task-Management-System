package com.demo.repository;

import com.demo.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task ,Long>
{
    List<Task> findByCompleted(boolean completed);
    Page<Task> findByUserId(Long userId , Pageable pageable);
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    Page<Task> findAll(Pageable pageable);
}
