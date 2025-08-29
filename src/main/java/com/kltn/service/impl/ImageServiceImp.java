package com.kltn.service.impl;

import com.kltn.dto.entity.ImageDto;
import com.kltn.exception.DataNotFoundException;
import com.kltn.exception.MyCustomException;
import com.kltn.mapper.ImageMapper;
import com.kltn.model.Image;
import com.kltn.model.Product;
import com.kltn.repository.ImageRepository;
import com.kltn.repository.ProductRepository;
import com.kltn.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageServiceImp implements ImageService {
    private final ImageMapper imageMapper;

    private final ImageRepository imageRepository;

    private final ProductRepository productRepository;

    @Override
    public ImageDto uploadFile(Long idProduct, MultipartFile file) {
        // Kiểm tra bài đăng có tồn tại không
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isPresent()) {
            // Lưu ảnh vào database
            Image image = storeImage(idProduct, file);
            // Tạo link để truy cập ảnh
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/image/")
                    .path(image.getId())
                    .toUriString();
            // Trả về thông tin ảnh
            return new ImageDto(image.getId(), image.getFileName(), file.getContentType(), fileDownloadUri, idProduct);
        } else {
            throw new DataNotFoundException("I can't not found productID " + idProduct);
        }
    }

    @Override
    public Image storeImage(Long idProduct, MultipartFile file) {
        // Lấy tên file và validate
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")) {
                throw new DataNotFoundException("I can't found file name in " + fileName);
            }
            // Tìm bài đăng
            Optional<Product> product = productRepository.findById(idProduct);

            // Tạo đối tượng Image và lưu
            Image image = new Image(fileName, file.getContentType(), file.getBytes(), product.get());
            return imageRepository.save(image);
        } catch (Exception e) {
            throw new MyCustomException("Error while i handle save image " + fileName + "!" + e);
        }
    }

    @Override
    public Image getImage(String imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new DataNotFoundException("Không tim thấy ảnh có id " + imageId));
    }

    @Override
    public List<String> getImageByIdProduct(Long idProduct) {
        // Tạo danh sách để chứa link ảnh
        List<String> uri = new ArrayList<>();
        // Tìm bài đăng
        Optional<Product> product = productRepository.findById(idProduct);
        // Tìm tất cả ảnh của bài đăng này
        List<Image> images = imageRepository.findImageByProduct(product.get());
        for (Image image : images) {
            uri.add(ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/image/")
                    .path(image.getId())
                    .toUriString());
        }
        return uri;
    }

    @Override
    public void deleteAllImages(Long idProduct) {
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isPresent()) {
            List<Image> images = imageRepository.findImageByProduct(product.get());
            imageRepository.deleteAll(images);
        } else {
            throw new DataNotFoundException("I can't not found productID " + idProduct);
        }
    }

    @Override
    public List<ImageDto> getImageDTOByIdProduct(Long idProduct) {
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isPresent()) {
            List<Image> images = imageRepository.findImageByProduct(product.get());
            List<ImageDto> imageDtos = new ArrayList<>();
            for (Image image : images) {
                ImageDto imageDto = imageMapper.toDto(image);
                imageDto.setUri(Base64.getEncoder().encodeToString(image.getData()));
                imageDtos.add(imageDto);
            }
            return imageDtos;
        } else {
            throw new DataNotFoundException("I can't not found productID " + idProduct);
        }
    }
}
