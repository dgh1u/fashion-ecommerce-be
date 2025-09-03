package com.kltn.service;

import com.kltn.dto.entity.ProductDto;
import com.kltn.dto.entity.SearchDto;
import com.kltn.dto.request.product.CreateProductRequest;
import com.kltn.dto.request.product.UpdateProductRequest;
import com.kltn.dto.response.product.*;
import com.kltn.dto.response.product.*;
import com.kltn.model.Product;
import com.kltn.repository.custom.CustomProductQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

public interface ProductService {
    Page<Product> getAllProduct(CustomProductQuery.ProductFilterParam param, PageRequest pageRequest);
    
    Page<ProductDto> getAllProductWithInventories(CustomProductQuery.ProductFilterParam param, PageRequest pageRequest);

    ProductDto getProductById(Long id);

    CreateProductResponse createProduct(CreateProductRequest createProductRequest, String email);

    UpdateProductResponse updateProduct(Long id, UpdateProductRequest updateProductRequest, String name);

    HiddenProductResponse hideProduct(Long id);

    DeleteProductResponse deleteProductByAdmin(Long id);

    ApproveProductResponse ApproveProduct(Long idProduct, String usernameApprove, boolean isApprove);

    Page<ProductDto> searchProductByMaps(SearchDto searchForm, int page, int sort);

    Page<ProductDto> getProductWaitingApprove(int page);

}
