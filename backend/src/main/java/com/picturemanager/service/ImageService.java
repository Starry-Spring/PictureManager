// src/main/java/com/picturemanager/service/ImageService.java (修复版本)
package com.picturemanager.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.picturemanager.config.FileStorageProperties;
import com.picturemanager.dto.ImageDTO;
import com.picturemanager.dto.ImageResponseDTO;
import com.picturemanager.dto.PaginatedResponse;
import com.picturemanager.entity.*;
import com.picturemanager.repository.ImageRepository;
import com.picturemanager.repository.TagRepository;
import com.picturemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FileStorageProperties fileStorageProperties;



    @Transactional
    public ImageResponseDTO uploadImage(Long userId, ImageDTO imageDTO) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        MultipartFile file = imageDTO.getFile();

        // 验证文件类型
        String contentType = file.getContentType();
        if (!fileStorageProperties.getAllowedTypes().contains(contentType)) {
            throw new RuntimeException("不支持的文件类型: " + contentType);
        }

        // 生成存储文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String storedFilename = UUID.randomUUID().toString() + fileExtension;

        // 创建用户目录
        String userDir = fileStorageProperties.getUploadDir() + File.separator + user.getId();
        Path userDirPath = Paths.get(userDir);
        if (!Files.exists(userDirPath)) {
            Files.createDirectories(userDirPath);
        }

        // 保存文件
        Path targetLocation = userDirPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 获取图片尺寸
        BufferedImage bufferedImage = ImageIO.read(targetLocation.toFile());
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // 创建图片实体
        Image image = new Image();
        image.setUser(user);
        image.setOriginalFilename(originalFilename);
        image.setStoredFilename(storedFilename);
        image.setFilePath(targetLocation.toString());
        image.setFileSize(file.getSize());
        image.setMimeType(contentType);
        image.setImageWidth(width);
        image.setImageHeight(height);
        image.setTitle(imageDTO.getTitle());
        image.setDescription(imageDTO.getDescription());
        image.setUploadedAt(LocalDateTime.now());
        image.setIsDeleted(false);

        // 处理标签
        if (imageDTO.getTags() != null && !imageDTO.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : imageDTO.getTags()) {
                Tag tag = tagRepository.findByNameAndCreatedBy(tagName, user)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setType(TagType.USER);
                            newTag.setCreatedBy(user);
                            newTag.setCreatedAt(LocalDateTime.now());
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            image.setTags(tags);
        }

        // 提取EXIF信息
        extractExifInfo(image, targetLocation.toFile());

        // 保存图片
        Image savedImage = imageRepository.save(image);

        return convertToResponseDTO(savedImage);
    }

    private void extractExifInfo(Image image, File imageFile) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

            ImageExif exif = new ImageExif();
            exif.setImage(image);

            // 相机信息
            ExifIFD0Directory ifd0Dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0Dir != null) {
                exif.setCameraMake(ifd0Dir.getString(ExifIFD0Directory.TAG_MAKE));
                exif.setCameraModel(ifd0Dir.getString(ExifIFD0Directory.TAG_MODEL));
            }

            // 拍摄信息
            ExifSubIFDDirectory subIfdDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (subIfdDir != null) {
                Date date = subIfdDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (date != null) {
                    exif.setTakenAt(LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault()));
                }
                exif.setExposureTime(subIfdDir.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                exif.setFNumber(subIfdDir.getString(ExifSubIFDDirectory.TAG_FNUMBER));
                exif.setIsoSpeed(subIfdDir.getInteger(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                exif.setFocalLength(subIfdDir.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
            }

            // GPS信息
            GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDir != null) {
                try {
                    com.drew.lang.GeoLocation geoLocation = gpsDir.getGeoLocation();
                    if (geoLocation != null) {
                        // 使用 BigDecimal.valueOf 而不是 new BigDecimal
                        exif.setLatitude(BigDecimal.valueOf(geoLocation.getLatitude()));
                        exif.setLongitude(BigDecimal.valueOf(geoLocation.getLongitude()));
                    }
                } catch (Exception e) {
                    // 忽略错误
                }
            }

            image.setExif(exif);

        } catch (Exception e) {
            // 如果没有EXIF信息，静默失败
        }
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ImageResponseDTO> getUserImages(Long userId, int page, int size, String sortBy, String direction) {
        return getUserImages(userId, page, size, sortBy, direction, null, null, null);
    }
    
    @Transactional(readOnly = true)
    public PaginatedResponse<ImageResponseDTO> getUserImages(Long userId, int page, int size, String sortBy, String direction, 
                                                              String keyword, String searchType, String tag) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Image> imagePage;
        
        // 处理标签过滤
        if (tag != null && !tag.isEmpty()) {
            imagePage = imageRepository.findByUserAndTag(user, tag, pageable);
        }
        // 处理关键词搜索
        else if (keyword != null && !keyword.isEmpty()) {
            String decodedKeyword = keyword;
            try {
                decodedKeyword = java.net.URLDecoder.decode(keyword, "UTF-8");
            } catch (Exception e) {
                // 如果解码失败，使用原始关键词
            }
            
            switch (searchType != null ? searchType : "all") {
                case "title":
                    imagePage = imageRepository.searchByTitle(user, decodedKeyword, pageable);
                    break;
                case "description":
                    imagePage = imageRepository.searchByDescription(user, decodedKeyword, pageable);
                    break;
                case "tag":
                    imagePage = imageRepository.searchByTagName(user, decodedKeyword, pageable);
                    break;
                default:
                    imagePage = imageRepository.searchAll(user, decodedKeyword, pageable);
                    break;
            }
        } else {
            imagePage = imageRepository.findByUserAndIsDeletedFalse(user, pageable);
        }

        List<ImageResponseDTO> content = imagePage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        // 修复这里的构造函数调用
        return new PaginatedResponse<>(
                content,
                imagePage.getNumber(),
                imagePage.getSize(),
                imagePage.getTotalElements(),
                imagePage.getTotalPages(),
                imagePage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ImageResponseDTO getImageById(Long userId, Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));

        if (!image.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问此图片");
        }

        return convertToResponseDTO(image);
    }

    @Transactional
    public void deleteImage(Long userId, Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));

        if (!image.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除此图片");
        }

        // 软删除
        image.setIsDeleted(true);
        imageRepository.save(image);
    }

    @Transactional
    public ImageResponseDTO updateImage(Long userId, Long imageId, ImageDTO imageDTO) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));

        if (!image.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权更新此图片");
        }

        image.setTitle(imageDTO.getTitle());
        image.setDescription(imageDTO.getDescription());

        // 更新标签
        if (imageDTO.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            User user = userRepository.findById(userId).orElseThrow();

            for (String tagName : imageDTO.getTags()) {
                Tag tag = tagRepository.findByNameAndCreatedBy(tagName, user)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setType(TagType.USER);
                            newTag.setCreatedBy(user);
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            image.setTags(tags);
        }

        Image updatedImage = imageRepository.save(image);
        return convertToResponseDTO(updatedImage);
    }

    @Transactional(readOnly = true)
    public List<ImageResponseDTO> getRecentImages(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Image> images = imageRepository.findRecentImages(user, PageRequest.of(0, limit));

        return images.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getUserTags(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Tag> tags = tagRepository.findByCreatedBy(user);

        return tags.stream()
                .map(Tag::getName)
                .distinct()
                .collect(Collectors.toList());
    }

    private ImageResponseDTO convertToResponseDTO(Image image) {
        ImageResponseDTO dto = new ImageResponseDTO();
        dto.setId(image.getId());
        dto.setTitle(image.getTitle());
        dto.setDescription(image.getDescription());
        dto.setOriginalFilename(image.getOriginalFilename());
        dto.setStoredFilename(image.getStoredFilename());
        dto.setFilePath(image.getFilePath());
        dto.setFileSize(image.getFileSize());
        dto.setMimeType(image.getMimeType());
        dto.setWidth(image.getImageWidth());
        dto.setHeight(image.getImageHeight());
        dto.setUploadedAt(image.getUploadedAt());

        // 标签
        if (image.getTags() != null) {
            Set<String> tagNames = image.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet());
            dto.setTags(tagNames);
        }

        // EXIF信息
        if (image.getExif() != null) {
            ImageExif exif = image.getExif();
            dto.setCameraMake(exif.getCameraMake());
            dto.setCameraModel(exif.getCameraModel());
            dto.setTakenAt(exif.getTakenAt());
            dto.setExposureTime(exif.getExposureTime());
            dto.setFNumber(exif.getFNumber());
            dto.setIsoSpeed(exif.getIsoSpeed());
            dto.setFocalLength(exif.getFocalLength());
            if (exif.getLatitude() != null) dto.setLatitude(exif.getLatitude().doubleValue());
            if (exif.getLongitude() != null) dto.setLongitude(exif.getLongitude().doubleValue());
        }

        return dto;
    }
}