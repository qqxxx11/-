package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dto.UserSearchDto;

import java.io.IOException;

public interface ArticleSearchService {
    /**
     * es文章搜索
     *
     * @param dto
     * @return
     */
    ResponseResult search(UserSearchDto dto) throws IOException;
}
