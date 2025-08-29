package com.kltn.service;

import com.kltn.dto.request.comment.CreateCommentRequest;
import com.kltn.dto.request.comment.UpdateCommentRequest;
import com.kltn.model.Comment;
import com.kltn.dto.entity.CommentDto;
import com.kltn.repository.custom.CustomCommentQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface CommentService {
    Page<Comment> getAllComment(CustomCommentQuery.CommentFilterParam param, PageRequest pageRequest);
    CommentDto createComment(CreateCommentRequest request,String email);
    CommentDto updateComment(UpdateCommentRequest request,String email);
    void deleteComment(Long id);
}
