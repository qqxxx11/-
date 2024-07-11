package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.search.pojo.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {
    /**
     * 生成静态文件上传到minio中
     *
     * @param apArticle
     * @param content
     */
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private Configuration configuration;
    @Autowired
    private ApArticleService articleService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Async
    public void buildArticleToMinIo(ApArticle apArticle, String content) {
        //参数有效性判断
        if (StringUtils.isNotBlank(content)) {
            Template template = null;
            try {
                //数据模型
                configuration.getTemplate("article.ftl");
                Map<String, Object> contentDate = new HashMap<>();
                contentDate.put("content", JSONArray.parseArray(content));
                StringWriter out = new StringWriter();
                //合成
                template.process(contentDate, out);

                //上传到minio
                String path = fileStorageService.uploadHtmlFile("", apArticle.getId() + ".html", new ByteArrayInputStream(out.toString().getBytes()));

                //保存minio中静态页地址
                articleService.update(Wrappers.<ApArticle>lambdaUpdate().eq(ApArticle::getId, apArticle.getId()).set(ApArticle::getStaticUrl, path));
                //发送消息创建索引
                createArticleEsIndex(apArticle, content, path);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 发送消息创建索引
     *
     * @param apArticle
     * @param content
     * @param path
     */

    private void createArticleEsIndex(ApArticle apArticle, String content, String path) {
        SearchArticleVo vo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle, vo);
        vo.setContent(content);
        vo.setStaticUrl(path);
        kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC, JSON.toJSONString(vo));
    }
}
