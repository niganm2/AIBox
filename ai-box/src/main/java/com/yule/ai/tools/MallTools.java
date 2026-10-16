package com.yule.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yule.ai.entity.po.Order;
import com.yule.ai.entity.po.Product;
import com.yule.ai.entity.po.Store;
import com.yule.ai.entity.query.ProductQuery;
import com.yule.ai.service.IOrderService;
import com.yule.ai.service.IProductService;
import com.yule.ai.service.IStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class MallTools {

    private final IProductService productService;
    private final IStoreService storeService;
    private final IOrderService orderService;

    @Tool(description = "根据条件查询商城商品信息，支持按类型筛选、按价格上限筛选、按价格或库存排序")
    public List<Product> queryProducts(
            @ToolParam(description = "查询条件，可包含商品类型(type)、最高价格(maxPrice)、排序方式(sorts)", required = false) ProductQuery query
    ) {
        if (query == null) {
            return productService.list();
        }

        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (query.getType() != null && !query.getType().isEmpty()) {
            wrapper.eq("type", query.getType());
        }
        if (query.getMaxPrice() != null) {
            wrapper.le("price", query.getMaxPrice());
        }

        if (query.getSorts() != null && !query.getSorts().isEmpty()) {
            for (ProductQuery.Sort sort : query.getSorts()) {
                wrapper.orderBy(true, sort.getAsc(), sort.getField());
            }
        }
        return productService.list(wrapper);
    }

    @Tool(description = "查询所有门店信息")
    public List<Store> queryStores() {
        return storeService.list();
    }

    @Tool(description = "创建商品订单，返回订单号")
    public Integer createOrder(
            @ToolParam(description = "订购的商品名称") String product,
            @ToolParam(description = "取货门店名称") String store,
            @ToolParam(description = "客户姓名") String customerName,
            @ToolParam(description = "联系电话") String contactInfo,
            @ToolParam(description = "备注信息", required = false) String remark) {
        Order order = new Order();
        order.setProduct(product);
        order.setStore(store);
        order.setCustomerName(customerName);
        order.setContactInfo(contactInfo);
        order.setRemark(remark);
        orderService.save(order);
        return order.getId();
    }

    @Tool(description = "根据订单号删除订单")
    public String deleteOrder(
            @ToolParam(description = "要删除的订单号") Integer orderId) {
        boolean removed = orderService.removeById(orderId);
        return removed ? "订单 " + orderId + " 已删除" : "订单 " + orderId + " 不存在，删除失败";
    }
}
