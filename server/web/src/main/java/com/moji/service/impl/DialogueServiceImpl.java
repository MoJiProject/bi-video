package com.moji.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.mapper.DialogueMapper;
import com.moji.po.Dialogue;
import com.moji.service.DialogueService;
import org.springframework.stereotype.Service;

@Service
public class DialogueServiceImpl extends ServiceImpl<DialogueMapper, Dialogue> implements DialogueService {
}
