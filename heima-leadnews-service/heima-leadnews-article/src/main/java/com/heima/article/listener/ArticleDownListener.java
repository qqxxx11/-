package com.heima.article.listener;

import com.alibaba.fastjson.JSONArray;
import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.WmNewsMessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ArticleDownListener {
    @Autowired
    private ApArticleConfigService apArticleConfigService;

    /**
     * 监听文章上下架消息
     *
     * @param messages
     */
    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void onMessages(String messages) {
        if (StringUtils.isNotBlank(messages)) {
            Map map = JSONArray.parseObject(messages, Map.class);
            apArticleConfigService.updateByMap(map);
        }
    }
}
