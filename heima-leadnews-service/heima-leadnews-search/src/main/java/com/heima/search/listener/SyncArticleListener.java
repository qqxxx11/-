package com.heima.search.listener;

import com.alibaba.fastjson.JSONArray;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.search.pojo.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SyncArticleListener {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 监听添加文章的消息来创建索引
     *
     * @param message
     */
    @KafkaListener(topics = ArticleConstants.ARTICLE_ES_SYNC_TOPIC)
    public void onMessage(String message) {
        log.info("接收到消息“{}", message);
        SearchArticleVo vo = JSONArray.parseObject(message, SearchArticleVo.class);
        IndexRequest indexRequest = new IndexRequest("app_info_article");
        indexRequest.id(vo.getId().toString());
        indexRequest.source(message, XContentType.JSON);
        try {
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("创建索引库错误：{}", message);
            throw new RuntimeException(e);
        }
    }
}
