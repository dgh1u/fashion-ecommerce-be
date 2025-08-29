package com.kltn.service.impl;

import com.kltn.dto.entity.*;
import com.kltn.dto.response.product.*;
import com.kltn.model.*;
import com.kltn.repository.*;
import com.kltn.dto.entity.*;
import com.kltn.dto.request.product.CreateProductRequest;
import com.kltn.dto.request.product.UpdateProductRequest;
import com.kltn.dto.response.product.*;
import com.kltn.exception.DataNotFoundException;
import com.kltn.mapper.CriteriaMapper;
import com.kltn.mapper.CommentMapper;
import com.kltn.mapper.ProductMapper;
import com.kltn.mapper.UserMapper;
import com.kltn.model.*;
import com.kltn.model.enums.ActionName;
import com.kltn.repository.*;
import com.kltn.repository.custom.CustomProductQuery;
import com.kltn.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private final CriteriaRepository criteriaRepository;

    private final CommentRepository commentRepository;

    private final ImageServiceImp imageServiceImp;

    private final DocumentServiceImpl documentServiceImpl;

    private final ActionServiceImp actionService;

    ; // Some Mapper in this
    private final ProductMapper productMapper;

    private final CriteriaMapper criteriaMapper;

    private final UserMapper userMapper;

    private final CommentMapper commentMapper;

    // hold
    @Override
    public Page<Product> getAllProduct(CustomProductQuery.ProductFilterParam param, PageRequest pageRequest) {
        try {
            Specification<Product> specification = CustomProductQuery.getFilterProduct(param);
            return productRepository.findAll(specification, pageRequest);
        } catch (Exception e) {
            throw new DataNotFoundException("Không có bài viết nào được tìm thấy! " + e.getMessage());
        }
    }

    // hold
    @Override
    public ProductDto getProductById(Long id) {
        // Tìm bài viết
        Optional<Product> product = productRepository.findProductById(id);

        // Kiểm tra xem bài viết có tồn tại không
        if (product.isPresent()) {
            ProductDto productDto = productMapper.toProductDto(product.get());
            // Lay cho o ra
            CriteriaDto criteriaDto = criteriaMapper.toCriteriaDto(product.get().getCriteria());
            // Lấy các bình luận của bài đăng
            List<CommentDto> commentDtos = new ArrayList<>();
            List<Comment> comments = commentRepository.findCommentsByProductId(id);
            for (Comment comment : comments) {
                commentDtos.add(commentMapper.toCommentDTO(comment));
            }
            // Lấy hình ảnh của bài đăng
            List<String> images = imageServiceImp.getImageByIdProduct(id);



            // Thiết lập dữ liệu cho DTO
            productDto.setCriteriaDTO(criteriaDto);
            productDto.setImageStrings(images);
            productDto.setCommentDTOS(commentDtos);
            productDto.setUserDTO(userMapper.toUserDto(product.get().getUser()));

            // Trả về thông tin bài viết
            return productDto;
        } else {
            // Nếu không tìm thấy bài viết
            throw new DataNotFoundException("Không tìm thấy bài viết theo id đã cho");
        }
    }

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
            product.setApproved(true);
            product.setNotApproved(true);

            // Xử lý đối tượng Criteria liên quan đến bài đăng
            Criteria criteria = criteriaMapper.toCriteria(createProductRequest.getCriteria());
            criteria.setId(null);
            criteria.setProduct(product);
            Criteria criteriaSaved = criteriaRepository.save(criteria);
            product.setCriteria(criteriaSaved);

            // Lưu bài đăng vào database
            Product productSaved = productRepository.save(product);

            // Tạo action cho bài đăng
            actionService.createAction(product, user, ActionName.CREATE);

            return productMapper.toCreateProductResponse(productSaved);
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

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
            product.setApproved(true);
            product.setNotApproved(true);

            // Gán Criteria vào Product (quan hệ 1-1)
            criteria.setProduct(product);

            // Lưu vào database
            criteriaRepository.save(criteria);
            productRepository.save(product);

            return productMapper.toUpdateProductResponse(product);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật bài đăng: {}", e.getMessage());
            throw new RuntimeException("Lỗi trong quá trình cập nhật bài đăng: " + e.getMessage());
        }
    }

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

    @Override
    public ApproveProductResponse ApproveProduct(Long idProduct, String usernameApprove, boolean isApprove) {
        try {
            Optional<Product> productOpt = productRepository.findById(idProduct);
            if (productOpt.isEmpty()) {
                return new ApproveProductResponse(idProduct, "Không tìm thấy bài đăng", false);
            }

            Optional<User> userOpt = userRepository.findByEmail(usernameApprove);
            if (userOpt.isEmpty()) {
                return new ApproveProductResponse(idProduct,
                        "Không tìm thấy người dùng có username: " + usernameApprove,
                        false);
            }

            Product product = productOpt.get();
            User user = userOpt.get();
            User productOwner = product.getUser(); // Chủ bài viết

            if (isApprove) {
                // Duyệt bài viết
                product.setApproved(true);
                product.setNotApproved(false);
                actionService.createAction(product, user, ActionName.APPROVE);
            } else {
                // Khóa bài viết
                // Kiểm tra trạng thái hiện tại của bài viết
                boolean wasWaitingApproval = product.getApproved() && product.getNotApproved(); // Chờ duyệt
                boolean wasApproved = product.getApproved() && !product.getNotApproved(); // Đã duyệt

                // Cập nhật trạng thái khóa bài
                product.setApproved(false);
                product.setNotApproved(true);

                // // Hoàn tiền nếu bài viết đang ở trạng thái "Chờ duyệt"
                // if (wasWaitingApproval) {
                // productOwner.setBalance(productOwner.getBalance() + 2000);
                // userRepository.save(productOwner);
                // log.info("Hoàn tiền 2000 cho user ID: {} khi khóa bài viết ID: {} từ trạng
                // thái chờ duyệt",
                // productOwner.getId(), idProduct);
                // }

                actionService.createAction(product, user, ActionName.BLOCK);
            }

            productRepository.save(product);

            String message = "Bài đăng đã được " + (isApprove ? "duyệt" : "khóa") + " thành công";
            // if (!isApprove && product.getApproved() && product.getNotApproved()) {
            // message += " và đã hoàn tiền 2000 cho chủ bài viết";
            // }

            return new ApproveProductResponse(idProduct, message, isApprove);

        } catch (Exception e) {
            log.error("Lỗi khi duyệt bài đăng: {}", e.getMessage());
            return new ApproveProductResponse(idProduct, "Đã xảy ra lỗi trong quá trình xử lý", false);
        }
    }

    @Override
    public Page<ProductDto> searchProductByMaps(SearchDto searchForm, int page, int sort) {
        return null;
    }

    @Override
    public Page<ProductDto> getProductWaitingApprove(int page) {
        // Lấy danh sách bài đăng chờ duyệt từ repository với phân trang
        Page<Product> products = productRepository.findByApprovedFalseAndNotApprovedFalse(
                PageRequest.of(page, 12, Sort.by("createAt").descending()));

        // Chuyển đổi từ Page<Product> thành Page<ProductDto>
        return products.map(productMapper::toProductDto);
    }
}
