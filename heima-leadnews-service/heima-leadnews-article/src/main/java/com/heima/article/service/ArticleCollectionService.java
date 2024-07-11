package com.heima.article.service;

import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ArticleCollectionService {

    /**
     * 收藏文章
     *
     * @param dto
     * @return
     */
    ResponseResult collection(CollectionBehaviorDto dto);

}
