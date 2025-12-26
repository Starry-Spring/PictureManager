// src/main/java/com/picturemanager/repository/TagRepository.java
package com.picturemanager.repository;

import com.picturemanager.entity.Tag;
import com.picturemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameAndCreatedBy(String name, User createdBy);

    List<Tag> findByCreatedBy(User createdBy);

    List<Tag> findByType(com.picturemanager.entity.TagType type);
}