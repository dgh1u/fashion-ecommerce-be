package com.kltn.service;

import com.kltn.dto.entity.ImageDto;
import com.kltn.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    ImageDto uploadFile(Long idProduct, MultipartFile file);

    List<ImageDto> uploadMultipleFiles(Long idProduct, List<MultipartFile> files);

    Image storeImage(Long idProduct, MultipartFile file);

    Image storeImageWithOrder(Long idProduct, MultipartFile file, Integer orderIndex);

    Image getImage(String imageId);

    List<String> getImageByIdProduct(Long idProduct);

    void deleteAllImages(Long idProduct);

    List<ImageDto> getImageDTOByIdProduct(Long idProduct);

    void updateImageOrder(Long idProduct, List<String> imageIds);
}
