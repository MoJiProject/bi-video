package com.moji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.mapper.FansMapper;
import com.moji.po.Fans;
import com.moji.service.FansService;
import org.springframework.stereotype.Service;

@Service
public class FansServiceImpl extends ServiceImpl<FansMapper, Fans> implements FansService {
}
