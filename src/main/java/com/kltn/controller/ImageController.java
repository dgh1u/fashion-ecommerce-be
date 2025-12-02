package com.kltn.controller;

import com.kltn.dto.entity.ImageDto;
import com.kltn.model.Image;
import com.kltn.service.impl.ImageServiceImp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@Api(value = "Tìm nhà trọ", description = "Quản lý hình ảnh")
public class ImageController {
    @Autowired
    private ImageServiceImp imageService;

    @ApiOperation(value = "Upload 1 hình ảnh cho một tin đăng")
    @PostMapping("/uploadImage/product/{idProduct}")
    public ImageDto uploadFile(@PathVariable Long idProduct, @RequestParam("file") MultipartFile file) {
        return imageService.uploadFile(idProduct, file);
    }

    @ApiOperation(value = "Delete hình ảnh một tin đăng")
    @DeleteMapping("/deleteImage/product/{idProduct}")
    public void deleteFile(@PathVariable Long idProduct) {
        imageService.deleteAllImages(idProduct);
    }

    @ApiOperation(value = "Upload nhiều hình ảnh cho một tin đăng với thứ tự")
    @PostMapping("/uploadMultipleFiles/product/{idProduct}")
    public List<ImageDto> uploadMultipleFiles(@PathVariable Long idProduct,
            @RequestParam("files") MultipartFile[] files) {
        return imageService.uploadMultipleFiles(idProduct, Arrays.asList(files));
    }

    @ApiOperation(value = "Cập nhật thứ tự hình ảnh cho một sản phẩm")
    @PutMapping("/updateImageOrder/product/{idProduct}")
    public ResponseEntity<String> updateImageOrder(@PathVariable Long idProduct, @RequestBody List<String> imageIds) {
        imageService.updateImageOrder(idProduct, imageIds);
        return ResponseEntity.ok("Cập nhật thứ tự hình ảnh thành công");
    }

    @ApiOperation(value = "Lấy danh sách hình ảnh của một tin đăng khi chỉnh sửa tin đăng")
    @GetMapping("/imageByte/product/{idProduct}")
    public List<ImageDto> getImageDTOByIdProduct(@PathVariable Long idProduct) {
        return imageService.getImageDTOByIdProduct(idProduct);
    }

    @ApiOperation(value = "Render 1 ảnh thành link")
    @GetMapping("/image/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        // Load file from database
        Image image = imageService.getImage(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .body(new ByteArrayResource(image.getData()));
    }

    @ApiOperation(value = "Lấy danh sách hình ảnh của một tin đăng khi xem chi tiết tin đăng")
    @GetMapping("/image/product/{idProduct}")
    public List<String> getImageByIdProduct(@PathVariable Long idProduct) {
        return imageService.getImageByIdProduct(idProduct);
    }
}
