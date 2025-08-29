package com.kltn.dto.request.criteria;

import com.kltn.repository.custom.CustomCriteriaQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetCriteriaRequest extends CustomCriteriaQuery.CriteriaFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;

    @Range(min = 5, max = 50, message = "Số lượng kết quả trên một trang là từ 5 đến 50")
    private int limit = 5;
}
