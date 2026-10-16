package com.yule.ai.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("orders")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订购商品
     */
    private String product;

    /**
     * 取货门店
     */
    private String store;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 联系电话
     */
    private String contactInfo;

    /**
     * 备注
     */
    private String remark;
}
