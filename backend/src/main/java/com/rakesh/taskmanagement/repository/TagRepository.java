package com.rakesh.taskmanagement.repository;

import com.rakesh.taskmanagement.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserId(Long userId);
    Optional<Tag> findByNameAndUserId(String name, Long userId);
}
