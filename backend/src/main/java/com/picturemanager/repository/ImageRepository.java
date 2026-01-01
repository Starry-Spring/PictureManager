// src/main/java/com/picturemanager/repository/ImageRepository.java
package com.picturemanager.repository;

import com.picturemanager.entity.Image;
import com.picturemanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    // 根据用户查找图片
    Page<Image> findByUserAndIsDeletedFalse(User user, Pageable pageable);

    // 查找用户的所有图片
    List<Image> findByUserAndIsDeletedFalse(User user);

    // 根据标签查找图片
    @Query("SELECT i FROM Image i JOIN i.tags t WHERE i.user = :user AND i.isDeleted = false AND t.name = :tagName")
    Page<Image> findByUserAndTag(@Param("user") User user, @Param("tagName") String tagName, Pageable pageable);

    // 搜索图片（标题或描述）- 修复版本
    @Query("SELECT i FROM Image i WHERE i.user = :user AND i.isDeleted = false AND " +
            "(i.title LIKE %:keyword% OR i.description LIKE %:keyword%)")
    Page<Image> searchByUser(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
    
    // 按标题搜索
    @Query("SELECT i FROM Image i WHERE i.user = :user AND i.isDeleted = false AND i.title LIKE %:keyword%")
    Page<Image> searchByTitle(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
    
    // 按描述搜索
    @Query("SELECT i FROM Image i WHERE i.user = :user AND i.isDeleted = false AND i.description LIKE %:keyword%")
    Page<Image> searchByDescription(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
    
    // 按标签名搜索
    @Query("SELECT DISTINCT i FROM Image i JOIN i.tags t WHERE i.user = :user AND i.isDeleted = false AND t.name LIKE %:keyword%")
    Page<Image> searchByTagName(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
    
    // 全局搜索（标题、描述、标签）
    @Query("SELECT DISTINCT i FROM Image i LEFT JOIN i.tags t WHERE i.user = :user AND i.isDeleted = false AND " +
            "(i.title LIKE %:keyword% OR i.description LIKE %:keyword% OR t.name LIKE %:keyword%)")
    Page<Image> searchAll(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

    // 按时间范围查找
    Page<Image> findByUserAndIsDeletedFalseAndUploadedAtBetween(User user, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 查找最新图片（用于轮播）
    @Query("SELECT i FROM Image i WHERE i.user = :user AND i.isDeleted = false ORDER BY i.uploadedAt DESC")
    List<Image> findRecentImages(@Param("user") User user, Pageable pageable);

    // 根据用户ID查找未删除的图片
    @Query("SELECT i FROM Image i WHERE i.user.id = :userId AND i.isDeleted = false")
    List<Image> findByUserIdAndIsDeletedFalse(@Param("userId") Long userId);
    
    // 查找用户的所有图片（包括已删除的）
    List<Image> findByUser(User user);
}