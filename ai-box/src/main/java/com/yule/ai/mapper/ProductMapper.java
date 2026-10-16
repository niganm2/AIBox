package com.yule.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yule.ai.entity.po.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
