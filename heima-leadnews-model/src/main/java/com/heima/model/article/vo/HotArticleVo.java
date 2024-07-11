package com.heima.model.article.vo;

import com.heima.model.article.pojos.ApArticle;
import lombok.Data;

@Data
public class HotArticleVo extends ApArticle {
    private Integer score;
}
