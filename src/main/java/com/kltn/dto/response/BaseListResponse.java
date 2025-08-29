package com.kltn.dto.response;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseListResponse<T> extends BaseResponse {

    private DataList data;

    public void setResult(List<T> rows, Integer total) {
        if (rows != null) {
            super.setSuccess(true);
            data = new DataList();
            data.setItems(rows);
            data.setTotal(total);
        }
    }

}
