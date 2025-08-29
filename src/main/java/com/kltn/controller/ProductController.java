package com.kltn.controller;

import com.kltn.config.JwtConfig;
import com.kltn.dto.entity.ProductDto;
import com.kltn.dto.request.product.CreateProductRequest;
import com.kltn.dto.request.product.GetProductRequest;
import com.kltn.dto.request.product.UpdateProductRequest;
import com.kltn.dto.response.BaseResponse;
import com.kltn.dto.response.Response;
import com.kltn.dto.response.product.UpdateProductResponse;
import com.kltn.mapper.ProductMapper;
import com.kltn.model.Product;
import com.kltn.repository.ProductRepository;
import com.kltn.service.impl.ProductServiceImp;
import com.kltn.service.impl.UserDetailServiceImp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(value = "Tìm nhà trọ")
public class ProductController {
    private final JwtConfig jwtConfig;

    private final UserDetailServiceImp userDetailServiceImp;

    private final ProductServiceImp productService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // Test
    @GetMapping("/product/hello-world")
    public String HelloWorld() {
        return "Hello World";
    }

    // hoàn thành
    @ApiOperation(value = "Lấy tất cả tin đăng")
    @GetMapping("/products")
    public ResponseEntity<?> getAllProduct(@Valid @ModelAttribute GetProductRequest request) {
        Page<Product> page = productService.getAllProduct(request,
                PageRequest.of(request.getStart(), request.getLimit()));
        return BaseResponse.successListData(
                page.getContent().stream().map(productMapper::toProductDto).collect(Collectors.toList()),
                (int) page.getTotalElements());
    }

    // hoàn thành
    @ApiOperation(value = "Lấy thông tin của một tin đăng")
    @GetMapping("/product/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductDto productDto = productService.getProductById(id);
            return BaseResponse.successData(productDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Đăng tin mới")
    @PostMapping("/product")
    public ResponseEntity<?> createProduct(@RequestHeader("Authorization") String token,
            @RequestBody @Valid CreateProductRequest createProductRequest) {
        try {

            String userId = jwtConfig.getUserIdFromJWT(token.split(" ")[1]);
            UserDetails userDetails = userDetailServiceImp.loadUserByUsername(userId);

            return BaseResponse
                    .successData(productService.createProduct(createProductRequest, userDetails.getUsername()));
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("Lỗi không xác định: " + e.getMessage(), null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @ApiOperation(value = "Duyệt/Khóa tin đăng")
    @PutMapping("/product/{id}/approve/{bool}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> approveProductAndLogging(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @PathVariable boolean bool) {
        try {
            String userId = jwtConfig.getUserIdFromJWT(token.split(" ")[1]);
            return BaseResponse.successData(productService.ApproveProduct(id, userId, bool)); // Trả về status 200 nếu
                                                                                              // duyệt hoặc khóa thành
                                                                                              // công
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("Lỗi không xác định: " + e.getMessage(), null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // ok
    @ApiOperation(value = "Cập nhật một tin đăng")
    @PutMapping("/product/{id}")
    public ResponseEntity<?> updateProduct(@RequestHeader("Authorization") String token, @PathVariable Long id,
            @RequestBody UpdateProductRequest updateProductRequest) {
        try {
            String userId = jwtConfig.getUserIdFromJWT(token.split(" ")[1]);
            UpdateProductResponse updatedProduct = productService.updateProduct(id, updateProductRequest, userId);
            return BaseResponse.successData(updatedProduct);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "Ẩn/Mở khóa một tin đăng")
    @PutMapping("/product/hide/{id}")
    public ResponseEntity<?> hideProduct(@PathVariable Long id) {
        return BaseResponse.successData(productService.hideProduct(id));
    }

    @ApiOperation(value = "Xóa một tin đăng")
    @DeleteMapping("/product/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteProductByAdmin(@PathVariable Long id) {
        return BaseResponse.successData(productService.deleteProductByAdmin(id));
    }

    @ApiOperation(value = "Lấy danh sách tin đăng của một người dùng")
    @GetMapping("/products/{idUser}")
    public ResponseEntity<?> getProductsByUser(@PathVariable Long idUser,
            @Valid @ModelAttribute GetProductRequest request) {
        // Gán idUser vào bộ lọc (đảm bảo GetProductRequest có trường userId hoặc chuyển
        // sang ProductFilterParam nếu cần)
        request.setUserId(idUser);

        // Gọi service với bộ lọc đã thiết lập
        Page<Product> page = productService.getAllProduct(request,
                PageRequest.of(request.getStart(), request.getLimit()));

        // Chuyển đổi và trả về kết quả
        List<ProductDto> productDtos = page.getContent().stream()
                .map(productMapper::toProductDto)
                .collect(Collectors.toList());

        return BaseResponse.successListData(productDtos, (int) page.getTotalElements());
    }

}
