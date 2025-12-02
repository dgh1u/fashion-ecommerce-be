package com.kltn.dto.request;

import com.kltn.repository.custom.CustomOrderQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetOrderRequest extends CustomOrderQuery.OrderFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;
    @Range(min = 5, max = 50, message = "Số lượng đơn hàng trong một trang là từ 5 đến 50 đơn hàng")
    private int limit = 10;
}