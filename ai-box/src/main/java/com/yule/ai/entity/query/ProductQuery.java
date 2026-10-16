package com.yule.ai.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

@Data
public class ProductQuery {

    @ToolParam(required = false, description = "商品类型：数码、服饰、食品、家居、其它")
    private String type;

    @ToolParam(required = false, description = "最高价格（分），筛选价格不超过该值的商品")
    private Long maxPrice;

    @ToolParam(required = false, description = "排序方式")
    private List<Sort> sorts;

    @Data
    public static class Sort {
        @ToolParam(required = false, description = "排序字段: price或stock")
        private String field;
        @ToolParam(required = false, description = "是否升序: true/false")
        private Boolean asc;
    }
}
