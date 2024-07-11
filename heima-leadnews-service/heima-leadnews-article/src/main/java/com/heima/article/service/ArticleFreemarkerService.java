package com.heima.article.service;


import com.heima.model.article.pojos.ApArticle;

public interface ArticleFreemarkerService {
    /**
     * 生成静态文件上传到minio中
     *
     * @param apArticle
     * @param Content
     */
    void buildArticleToMinIo(ApArticle apArticle, String Content);
}
