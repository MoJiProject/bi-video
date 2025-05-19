package com.moji.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moji.R;
import com.moji.mapper.KeyWordMapper;
import com.moji.po.KeyWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/keyWord")
public class KeyWordController {


    @Autowired
    private KeyWordMapper keyWordMapper;

    /**
     * 获取热词
     * @return
     */
    @GetMapping("/getFireWord")
    public R<List<KeyWord>> fireWord(){

        LambdaQueryWrapper<KeyWord> wrapper=new LambdaQueryWrapper<>();
        wrapper.orderByDesc(KeyWord::getCount);
        List<KeyWord> keyWords = keyWordMapper.selectList(wrapper);
        List<KeyWord> keyWords1=new ArrayList<>();
        int a=0;
        for (KeyWord word : keyWords) {

            keyWords1.add(word);
            a++;
            if(a==10)
                break;
        }
        return R.success(keyWords1);
    }

    /**
     * 获取搜索词
     * @param keyWord
     * @return
     */
    @GetMapping("/getKeyWord")
    public R<List<KeyWord>> keyWord(@RequestParam String keyWord){

        LambdaQueryWrapper<KeyWord> wrapper=new LambdaQueryWrapper<>();
        wrapper.orderByDesc(KeyWord::getCount);
        wrapper.likeRight(true,KeyWord::getWord,keyWord);
        List<KeyWord> keyWords = keyWordMapper.selectList(wrapper);
        List<KeyWord> keyWords1=new ArrayList<>();
        int a=0;
        for (KeyWord word : keyWords) {

            if(keyWord.length()>word.getWord().length())
                continue;

            String wordWord = word.getWord();

            if (wordWord.startsWith(keyWord)) {
                // 计算 prefix 的结束位置
                int startIndex = keyWord.length();
                // 提取出子字符串
                String result = wordWord.substring(startIndex);
              word.setWord(result);
            keyWords1.add(word);}
            a++;
            if(a==10)
                break;
        }
        return R.success(keyWords1);
    }

    /**
     * 添加搜索词
     * @param keyWord
     * @return
     */
    @GetMapping("/addKeyWord")
    public R<String> insertKeyWord(@RequestParam String keyWord){

        if(keyWord.length()<=1)
            return R.error("长度不够");

        LambdaQueryWrapper<KeyWord> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(KeyWord::getWord,keyWord);
        KeyWord keyWord1 = keyWordMapper.selectOne(wrapper);

        KeyWord keyWord2=new KeyWord();
        keyWord2.setWord(keyWord);
        if(keyWord1==null)
            keyWordMapper.insert(keyWord2);
        else {
            keyWord1.setCount(keyWord1.getCount() + 1);
            keyWordMapper.updateById(keyWord1);
        }
        return R.success("添加成功");
    }


    /**
     * 获取热词
     * @return
     */
    @GetMapping("/placeholder")
    public R<KeyWord> placeholder(){

        List<KeyWord> keyWords = keyWordMapper.selectList(null);
        if(!keyWords.isEmpty()) {
            Random random = new Random();
            int i = random.nextInt(keyWords.size());
            return R.success(keyWords.get(i));
        }
        return R.error("未知错误");

    }
}
