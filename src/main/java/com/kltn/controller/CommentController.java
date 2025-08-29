package com.kltn.controller;

import com.kltn.config.JwtConfig;
import com.kltn.dto.request.comment.CreateCommentRequest;
import com.kltn.dto.request.comment.GetCommentRequest;
import com.kltn.dto.request.comment.UpdateCommentRequest;
import com.kltn.dto.response.BaseResponse;
import com.kltn.model.Comment;
import com.kltn.mapper.CommentMapper;
import com.kltn.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(value = "Quản lý bình luận", description = "Quản lý bình luận cho product")
public class CommentController {
    private final JwtConfig jwtConfig;
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @ApiOperation(value = "Lấy các bình luận của một product")
    @GetMapping("/comments")
    public ResponseEntity<?> getAllComment(@Valid @ModelAttribute GetCommentRequest request) {
        Page<Comment> page = commentService.getAllComment(request,
                PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(commentMapper::toCommentDTO)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Đăng một bình luận mới")
    @PostMapping("/comment")
    public ResponseEntity<?> createComment(@Valid @RequestBody CreateCommentRequest request,
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String email = jwtConfig.getUserIdFromJWT(jwt);
        return ResponseEntity.ok(commentService.createComment(request, email));
    }

    @ApiOperation(value = "Chỉnh sửa một bình luận")
    @PutMapping("/comment")
    public ResponseEntity<?> updateComment(@Valid @RequestBody UpdateCommentRequest request,
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String email = jwtConfig.getUserIdFromJWT(jwt);
        return ResponseEntity.ok(commentService.updateComment(request, email));
    }

    @ApiOperation(value = "Xóa một bình luận")
    @DeleteMapping("/comment/{id}")

    public ResponseEntity<?> deleteComment(@PathVariable("id") Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok("Xóa bình luận thành công");
    }
}