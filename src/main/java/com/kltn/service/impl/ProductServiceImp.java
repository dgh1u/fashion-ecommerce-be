package com.kltn.service.impl;

import com.kltn.dto.entity.*;
import com.kltn.dto.request.product.CreateProductRequest;
import com.kltn.dto.request.product.UpdateProductRequest;
import com.kltn.dto.response.product.*;
import com.kltn.exception.DataNotFoundException;
import com.kltn.mapper.CriteriaMapper;
import com.kltn.mapper.ProductMapper;
import com.kltn.mapper.ProductInventoryMapper;
import com.kltn.mapper.UserMapper;
import com.kltn.model.*;
import com.kltn.repository.*;
import com.kltn.repository.custom.CustomProductQuery;
import com.kltn.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImp implements ProductService {
    // Inject Service
    private final ApplicationEventPublisher applicationEventPublisher;
    // Inject Repository into class
    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final SizeRepository sizeRepository;

    private final ProductInventoryRepository productInventoryRepository;

    private final CriteriaRepository criteriaRepository;

    private final ImageServiceImp imageServiceImp;



    ; // Some Mapper in this
    private final ProductMapper productMapper;

    private final CriteriaMapper criteriaMapper;

    private final UserMapper userMapper;

    private final ProductInventoryMapper productInventoryMapper;

    /**
     * Lấy tất cả sản phẩm với phân trang và filter
     * Sử dụng Specification để lọc theo các tiêu chí
     */
    @Override
    public Page<Product> getAllProduct(CustomProductQuery.ProductFilterParam param, PageRequest pageRequest) {
        try {
            Specification<Product> specification = CustomProductQuery.getFilterProduct(param);
            return productRepository.findAll(specification, pageRequest);
        } catch (Exception e) {
            throw new DataNotFoundException("Không có bài viết nào được tìm thấy! " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả sản phẩm kèm thông tin tồn kho
     * Tránh N+1 query bằng cách fetch tất cả inventories một lần
     * Trả về ProductDto với đầy đủ thông tin
     */
    @Override
    @Transactional
    public Page<ProductDto> getAllProductWithInventories(CustomProductQuery.ProductFilterParam param,
            PageRequest pageRequest) {
        try {
            // Lấy page của Product
            Page<Product> productPage = getAllProduct(param, pageRequest);

            // Lấy tất cả productIds
            List<Long> productIds = productPage.getContent().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            // Lấy tất cả inventories cho các products này (để tránh N+1 problem)
            List<ProductInventory> allInventories = productInventoryRepository.findByProductIdIn(productIds);

            // Group inventories by productId
            Map<Long, List<ProductInventory>> inventoriesMap = allInventories.stream()
                    .collect(Collectors.groupingBy(inventory -> inventory.getProduct().getId()));

            // Convert to ProductDto với inventories
            List<ProductDto> productDtos = productPage.getContent().stream()
                    .map(product -> {
                        // Sử dụng mapper cơ bản trước
                        ProductDto dto = new ProductDto();
                        dto.setId(product.getId());
                        dto.setTitle(product.getTitle());
                        dto.setContent(product.getContent());
                        dto.setCreateAt(product.getCreateAt());
                        dto.setLastUpdate(product.getLastUpdate());
                        dto.setDel(product.getDel() != null ? product.getDel() : false);
                        dto.setType(product.getType());

                        // Map user if exists
                        if (product.getUser() != null) {
                            dto.setUserDTO(userMapper.toUserDto(product.getUser()));
                        }

                        // Map criteria if exists
                        if (product.getCriteria() != null) {
                            dto.setCriteriaDTO(criteriaMapper.toCriteriaDto(product.getCriteria()));
                        }

                        // Set inventories cho product này
                        List<ProductInventory> productInventories = inventoriesMap.getOrDefault(product.getId(),
                                new ArrayList<>());
                        List<ProductInventoryDto> inventoryDtos = productInventories.stream()
                                .map(productInventoryMapper::toProductInventoryDto)
                                .collect(Collectors.toList());
                        dto.setInventories(inventoryDtos);

                        return dto;
                    })
                    .collect(Collectors.toList());

            // Tạo Page<ProductDto> từ Page<Product>
            return new PageImpl<>(productDtos, pageRequest, productPage.getTotalElements());

        } catch (Exception e) {
            throw new DataNotFoundException("Không có bài viết nào được tìm thấy! " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết sản phẩm theo ID
     * Bao gồm thông tin criteria, images và inventories
     */
    @Override
    @Transactional
    public ProductDto getProductById(Long id) {
        // Tìm bài viết
        Optional<Product> product = productRepository.findProductById(id);

        // Kiểm tra xem bài viết có tồn tại không
        if (product.isPresent()) {
            ProductDto productDto = productMapper.toProductDto(product.get());
            // Lay cho o ra
            CriteriaDto criteriaDto = criteriaMapper.toCriteriaDto(product.get().getCriteria());
            // Lấy hình ảnh của bài đăng
            List<String> images = imageServiceImp.getImageByIdProduct(id);

            // Lấy thông tin inventory của sản phẩm
            List<ProductInventory> inventories = productInventoryRepository.findByProductId(id);

            // Thiết lập dữ liệu cho DTO
            productDto.setCriteriaDTO(criteriaDto);
            productDto.setImageStrings(images);
            productDto.setUserDTO(userMapper.toUserDto(product.get().getUser()));

            // Set inventories
            List<ProductInventoryDto> inventoryDtos = inventories.stream()
                    .map(productInventoryMapper::toProductInventoryDto)
                    .collect(Collectors.toList());
            productDto.setInventories(inventoryDtos);

            // Trả về thông tin bài viết
            return productDto;
        } else {
            // Nếu không tìm thấy bài viết
            throw new DataNotFoundException("Không tìm thấy bài viết theo id đã cho");
        }
    }

    /**
     * Tạo sản phẩm mới
     * Tạo product, criteria và inventory cho sản phẩm
     */
    @Override
    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest createProductRequest, String email) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                throw new DataNotFoundException("User not found with email: " + email);
            }
            User user = userOptional.get();

            // // Kiểm tra số dư và trừ đi 2000 nếu đủ
            // if (user.getBalance() < 2000) {
            // throw new DataNotFoundException("Số dư không đủ để đăng bài. Yêu cầu tối
            // thiểu là 2000.");
            // }
            // user.setBalance(user.getBalance() - 2000);
            userRepository.save(user);

            // Tạo bài đăng mới
            Product product = productMapper.createRequestDtoToProduct(createProductRequest);
            product.setCreateAt(LocalDateTime.now());
            product.setLastUpdate(LocalDateTime.now());
            product.setUser(user);
            product.setDel(false);

            // Xử lý đối tượng Criteria liên quan đến bài đăng
            Criteria criteria = criteriaMapper.toCriteria(createProductRequest.getCriteria());
            criteria.setId(null);
            criteria.setProduct(product);
            Criteria criteriaSaved = criteriaRepository.save(criteria);
            product.setCriteria(criteriaSaved);

            // Lưu bài đăng vào database
            Product productSaved = productRepository.save(product);

            // Xử lý inventory nếu có
            if (createProductRequest.getInventory() != null && !createProductRequest.getInventory().isEmpty()) {
                createProductInventory(productSaved, createProductRequest.getInventory());
            }

            return productMapper.toCreateProductResponse(productSaved);
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Tạo tồn kho cho sản phẩm mới
     * Lưu thông tin số lượng theo từng size
     */
    private void createProductInventory(Product product, Map<Long, Integer> inventoryMap) {
        for (Map.Entry<Long, Integer> entry : inventoryMap.entrySet()) {
            Long sizeId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity != null && quantity > 0) {
                Optional<Size> sizeOptional = sizeRepository.findById(sizeId);
                if (sizeOptional.isPresent()) {
                    ProductInventory inventory = new ProductInventory();
                    inventory.setProduct(product);
                    inventory.setSize(sizeOptional.get());
                    inventory.setQuantity(quantity);
                    inventory.setCreatedAt(LocalDateTime.now());
                    inventory.setUpdatedAt(LocalDateTime.now());

                    productInventoryRepository.save(inventory);
                }
            }
        }
    }

    /**
     * Cập nhật tồn kho của sản phẩm
     * Cập nhật hoặc tạo mới inventory cho từng size
     * Xóa các inventory không còn trong danh sách
     */
    private void updateProductInventory(Product product, Map<Long, Integer> inventoryMap) {
        // Approach 1: Update existing inventories và tạo mới nếu cần
        for (Map.Entry<Long, Integer> entry : inventoryMap.entrySet()) {
            Long sizeId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity != null && quantity >= 0) { // Cho phép quantity = 0 để xóa
                Optional<Size> sizeOptional = sizeRepository.findById(sizeId);
                if (sizeOptional.isPresent()) {
                    // Tìm inventory hiện có
                    Optional<ProductInventory> existingInventory = productInventoryRepository
                            .findByProductIdAndSizeId(product.getId(), sizeId);

                    if (existingInventory.isPresent()) {
                        // Cập nhật inventory hiện có
                        ProductInventory inventory = existingInventory.get();
                        if (quantity > 0) {
                            inventory.setQuantity(quantity);
                            inventory.setUpdatedAt(LocalDateTime.now());
                            productInventoryRepository.save(inventory);
                        } else {
                            // Xóa nếu quantity = 0
                            productInventoryRepository.delete(inventory);
                        }
                    } else if (quantity > 0) {
                        // Tạo inventory mới nếu quantity > 0
                        ProductInventory inventory = new ProductInventory();
                        inventory.setProduct(product);
                        inventory.setSize(sizeOptional.get());
                        inventory.setQuantity(quantity);
                        inventory.setCreatedAt(LocalDateTime.now());
                        inventory.setUpdatedAt(LocalDateTime.now());
                        productInventoryRepository.save(inventory);
                    }
                }
            }
        }

        // Xóa các inventory không có trong inventoryMap (các size không được gửi lên)
        List<ProductInventory> allExistingInventories = productInventoryRepository.findByProductId(product.getId());
        for (ProductInventory existing : allExistingInventories) {
            Long existingSizeId = existing.getSize().getId();
            if (!inventoryMap.containsKey(existingSizeId)) {
                productInventoryRepository.delete(existing);
            }
        }
    }

    /**
     * Cập nhật thông tin sản phẩm
     * Cập nhật title, content, criteria và inventory
     */
    @Override
    @Transactional
    public UpdateProductResponse updateProduct(Long id, UpdateProductRequest updateProductRequest, String userId) {
        try {
            // Kiểm tra xem bài đăng có tồn tại không
            Product product = productRepository.findProductById(id)
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy bài đăng với ID: " + id));

            // Tìm hoặc tạo Size
            Size size = sizeRepository
                    .findSizeById(updateProductRequest.getCriteria().getSize().getId())
                    .orElseGet(() -> {
                        Size newSize = new Size();
                        newSize.setName("Default Size");

                        return sizeRepository.save(newSize);
                    });

            // Cập nhật thông tin Criteria
            Criteria criteria = criteriaMapper.toCriteria(updateProductRequest.getCriteria());

            // Cập nhật thông tin Product
            product.setTitle(updateProductRequest.getTitle());
            product.setContent(updateProductRequest.getContent());
            product.setLastUpdate(LocalDateTime.now());
            product.setCriteria(criteria);

            // Gán Criteria vào Product (quan hệ 1-1)
            criteria.setProduct(product);

            // Lưu vào database
            criteriaRepository.save(criteria);
            productRepository.save(product);

            // Xử lý inventory nếu có (thêm logic này)
            if (updateProductRequest.getInventory() != null && !updateProductRequest.getInventory().isEmpty()) {
                updateProductInventory(product, updateProductRequest.getInventory());
            }

            return productMapper.toUpdateProductResponse(product);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật bài đăng: {}", e.getMessage());
            throw new RuntimeException("Lỗi trong quá trình cập nhật bài đăng: " + e.getMessage());
        }
    }

    /**
     * Ẩn/hiện sản phẩm
     * Chuyển đổi trạng thái del (true/false)
     */
    @Override
    public HiddenProductResponse hideProduct(Long id) {
        try {
            // Tìm bài đăng, nếu không có thì ném DataNotFoundException
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy bài đăng với ID " + id));

            // Chuyển đổi trạng thái của thuộc tính del (nếu false -> true, nếu true ->
            // false)
            product.setDel(!product.getDel());
            productRepository.save(product);

            // Tạo thông báo phù hợp dựa trên trạng thái mới của del
            String statusMessage = product.getDel() ? "Bài đăng đã được ẩn thành công."
                    : "Bài đăng đã được hiển thị thành công.";
            return new HiddenProductResponse(product.getId(), statusMessage, product.getDel());
        } catch (DataNotFoundException e) {
            log.warn("Không tìm thấy bài đăng với ID: {}", id);
            throw e; // Ném lỗi tiếp để controller xử lý
        } catch (Exception e) {
            log.error("Lỗi khi ẩn/bật bài đăng ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi ẩn/bật bài đăng.");
        }
    }

    /**
     * Xóa sản phẩm bởi Admin
     * Xóa vĩnh viễn sản phẩm khỏi hệ thống
     */
    @Override
    public DeleteProductResponse deleteProductByAdmin(Long id) {
        try {
            // Tìm bài đăng, nếu không có thì ném DataNotFoundException
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy bài đăng với ID " + id));

            // Xóa bài đăng
            productRepository.delete(product);

            // Trả về response
            return new DeleteProductResponse(id, "Bài đăng đã bị xóa bởi Admin.", true);
        } catch (DataNotFoundException e) {
            log.warn("Không tìm thấy bài đăng với ID: {}", id);
            throw e; // Ném lỗi để controller xử lý
        } catch (Exception e) {
            log.error("Lỗi khi Admin xóa bài đăng ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi xóa bài đăng.");
        }
    }

    /**
     * Tìm kiếm sản phẩm theo bản đồ
     * Chức năng chưa được triển khai
     */
    @Override
    public Page<ProductDto> searchProductByMaps(SearchDto searchForm, int page, int sort) {
        return null;
    }
}
