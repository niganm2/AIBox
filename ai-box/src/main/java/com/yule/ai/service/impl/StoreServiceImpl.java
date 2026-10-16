package com.yule.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yule.ai.entity.po.Store;
import com.yule.ai.mapper.StoreMapper;
import com.yule.ai.service.IStoreService;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements IStoreService {
}
