package com.kltn.controller;

import com.kltn.dto.entity.ActionDto;
import com.kltn.dto.request.GetActionRequest;
import com.kltn.dto.response.BaseResponse;
import com.kltn.mapper.ActionMapper;
import com.kltn.model.Action;
import com.kltn.service.impl.ActionServiceImp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
@Api(value = "Tìm nhà trọ", description = "Xem các hoạt động của website")
public class ActionController {


    private final ActionServiceImp actionService;


    private final ActionMapper actionMapper;

    @ApiOperation(value = "Lấy một trang các hoạt động")
    @GetMapping
    public ResponseEntity<?> getAction(@Valid @ModelAttribute GetActionRequest request) {
        Page<Action> page = actionService.getAction(request, PageRequest.of(request.getStart(), request.getLimit()));
        List<ActionDto> data = page.getContent()
                .stream()
                .map(actionMapper::toActionDto)
                .collect(Collectors.toList());
        return BaseResponse.successListData(data, (int) page.getTotalElements());

    }

    @ApiOperation(value = "Thay đổi trạng thái xem của hoạt động")
    @PutMapping("/{id}/mark-read")
    public ResponseEntity<?> markActionAsRead(@PathVariable Long id) {
        actionService.markActionAsRead(id);
        return BaseResponse.successData("Thông báo đã được đánh dấu là đã đọc");
    }

}

