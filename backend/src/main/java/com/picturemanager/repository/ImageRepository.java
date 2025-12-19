package com.picturemanager.repository;

import com.picturemanager.entity.Image;
import com.picturemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUser(User user);
    Optional<Image> findByIdAndUser(Long id, User user);
    List<Image> findByTitleContaining(String keyword);
}