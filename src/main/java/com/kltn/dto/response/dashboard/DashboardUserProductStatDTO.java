package com.kltn.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardUserProductStatDTO {
    private String groupKey;
    private long totalProducts;
}