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

    /**
     * Upload một file ảnh cho sản phẩm
     * Lưu ảnh vào database và trả về thông tin ảnh cùng link truy cập
     */
    @Override
    public ImageDto uploadFile(Long idProduct, MultipartFile file) {
        // Kiểm tra bài đăng có tồn tại không
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isPresent()) {
            // Lưu ảnh vào database với thứ tự tự động
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

    /**
     * Upload nhiều file ảnh cho sản phẩm
     * Lưu các ảnh với thứ tự cụ thể và trả về danh sách thông tin ảnh
     */
    @Override
    public List<ImageDto> uploadMultipleFiles(Long idProduct, List<MultipartFile> files) {
        // Kiểm tra bài đăng có tồn tại không
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isEmpty()) {
            throw new DataNotFoundException("I can't not found productID " + idProduct);
        }

        List<ImageDto> imageDtos = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            // Lưu ảnh với thứ tự cụ thể
            Image image = storeImageWithOrder(idProduct, file, i);
            // Tạo link để truy cập ảnh
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/image/")
                    .path(image.getId())
                    .toUriString();
            // Thêm vào danh sách kết quả
            imageDtos.add(new ImageDto(image.getId(), image.getFileName(), file.getContentType(), fileDownloadUri,
                    idProduct));
        }
        return imageDtos;
    }

    /**
     * Lưu ảnh vào database với thứ tự tự động
     * Tìm thứ tự tiếp theo dựa trên số ảnh hiện có của sản phẩm
     */
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
            if (product.isEmpty()) {
                throw new DataNotFoundException("I can't not found productID " + idProduct);
            }

            // Lấy thứ tự tiếp theo cho ảnh mới
            Integer nextOrderIndex = imageRepository.findMaxOrderIndexByProduct(product.get()) + 1;

            // Tạo đối tượng Image và lưu
            Image image = new Image(fileName, file.getContentType(), file.getBytes(), product.get(), nextOrderIndex);
            return imageRepository.save(image);
        } catch (Exception e) {
            throw new MyCustomException("Error while i handle save image " + fileName + "!" + e);
        }
    }

    /**
     * Lưu ảnh vào database với thứ tự được chỉ định
     * Sử dụng khi cần kiểm soát thứ tự ảnh cụ thể
     */
    @Override
    public Image storeImageWithOrder(Long idProduct, MultipartFile file, Integer orderIndex) {
        // Lấy tên file và validate
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")) {
                throw new DataNotFoundException("I can't found file name in " + fileName);
            }
            // Tìm bài đăng
            Optional<Product> product = productRepository.findById(idProduct);
            if (product.isEmpty()) {
                throw new DataNotFoundException("I can't not found productID " + idProduct);
            }

            // Tạo đối tượng Image với thứ tự cụ thể và lưu
            Image image = new Image(fileName, file.getContentType(), file.getBytes(), product.get(), orderIndex);
            return imageRepository.save(image);
        } catch (Exception e) {
            throw new MyCustomException("Error while i handle save image " + fileName + "!" + e);
        }
    }

    /**
     * Lấy thông tin ảnh theo ID
     * Trả về đối tượng Image chứa dữ liệu ảnh
     */
    @Override
    public Image getImage(String imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new DataNotFoundException("Không tim thấy ảnh có id " + imageId));
    }

    /**
     * Lấy danh sách link URI của tất cả ảnh thuộc sản phẩm
     * Trả về danh sách các đường dẫn để truy cập ảnh
     */
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

    /**
     * Xóa tất cả ảnh của sản phẩm
     * Dùng khi cần xóa toàn bộ ảnh liên quan đến sản phẩm
     */
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

    /**
     * Lấy danh sách DTO của ảnh thuộc sản phẩm
     * Bao gồm dữ liệu ảnh được mã hóa Base64, đã sắp xếp theo thứ tự
     */
    @Override
    public List<ImageDto> getImageDTOByIdProduct(Long idProduct) {
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isPresent()) {
            // Lấy danh sách ảnh đã sắp xếp theo thứ tự
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

    /**
     * Cập nhật thứ tự hiển thị của các ảnh trong sản phẩm
     * Nhận danh sách ID ảnh theo thứ tự mới và cập nhật orderIndex
     */
    @Override
    public void updateImageOrder(Long idProduct, List<String> imageIds) {
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isEmpty()) {
            throw new DataNotFoundException("I can't not found productID " + idProduct);
        }

        // Cập nhật thứ tự cho từng ảnh
        for (int i = 0; i < imageIds.size(); i++) {
            String imageId = imageIds.get(i);
            Optional<Image> imageOptional = imageRepository.findById(imageId);
            if (imageOptional.isPresent()) {
                Image image = imageOptional.get();
                // Kiểm tra ảnh có thuộc product này không
                if (image.getProduct().getId().equals(idProduct)) {
                    image.setOrderIndex(i);
                    imageRepository.save(image);
                }
            }
        }
    }
}
