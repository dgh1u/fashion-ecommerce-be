package com.kltn.mapper;

import com.kltn.dto.request.comment.CreateCommentRequest;
import com.kltn.dto.request.comment.UpdateCommentRequest;
import com.kltn.model.Comment;
import com.kltn.dto.entity.CommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface CommentMapper {

    // Chuyển từ CreateCommentRequest sang Comment
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastUpdate", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Comment toCreateComment(CreateCommentRequest request);

    // Chuyển từ UpdateCommentRequest sang Comment
    // Có thể không ánh xạ id, user, product vì đã có ở entity hiện tại
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Comment toUpdateComment(UpdateCommentRequest request);

    // Chuyển từ Comment sang CommentDTO
    @Mapping(target = "idProduct", source = "product.id")
    @Mapping(target = "userDTO", source = "user")
    CommentDto toCommentDTO(Comment comment);
}
