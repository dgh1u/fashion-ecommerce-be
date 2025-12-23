package com.kltn.mapper;

import com.kltn.dto.entity.CriteriaDto;
import com.kltn.dto.entity.ProductDto;
import com.kltn.dto.request.product.CreateProductRequest;
import com.kltn.dto.response.product.CreateProductResponse;
import com.kltn.dto.response.product.UpdateProductResponse;
import com.kltn.model.Product;
import com.kltn.model.Criteria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", uses = { UserMapper.class, CriteriaMapper.class,
        ProductInventoryMapper.class })
public interface ProductMapper {

    @Mapping(target = "userDTO", source = "user")
    @Mapping(target = "criteriaDTO", source = "criteria")

    ProductDto toProductDto(Product product);

    Product toProduct(ProductDto productDto);

    // Chuyển đổi từ CreateProductRequest sang Product
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "del", ignore = true)
    @Mapping(target = "lastUpdate", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "user", ignore = true)
    Product createRequestDtoToProduct(CreateProductRequest createProductRequest);

    // Fix: Kiểm tra lại property mapping
    @Mapping(target = "criteriaDTO", source = "criteria")
    @Mapping(target = "userDTO", source = "user")
    UpdateProductResponse toUpdateProductResponse(Product product);

    // Fix: Sử dụng expression để handle null safety
    @Mapping(target = "user", expression = "java(product.getUser() != null ? product.getUser().getEmail() : null)")
    @Mapping(target = "criteriaId", expression = "java(product.getCriteria() != null ? product.getCriteria().getId() : null)")
    @Mapping(target = "createAt", source = "createAt")
    @Mapping(target = "lastUpdate", source = "lastUpdate")
    CreateProductResponse toCreateProductResponse(Product product);

    // Alternative approach using conditional mapping
    // Nếu bạn muốn sử dụng cách tiếp cận khác, có thể dùng:
    /*
     * @Mapping(target = "user", source = "user.email", conditionExpression =
     * "java(product.getUser() != null)")
     * 
     * @Mapping(target = "criteriaId", source = "criteria.id", conditionExpression =
     * "java(product.getCriteria() != null)")
     * 
     * @Mapping(target = "createAt", source = "createAt")
     * 
     * @Mapping(target = "lastUpdate", source = "lastUpdate")
     * CreateProductResponse toCreateProductResponseAlternative(Product product);
     */
}