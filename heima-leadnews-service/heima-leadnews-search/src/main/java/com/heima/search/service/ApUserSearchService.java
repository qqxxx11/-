package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.search.dtos.HistorySearchDto;

public interface ApUserSearchService {
    /**
     * 保存用户搜索记录
     *
     * @param keyword 搜索关键词
     * @param userId  用户ID
     */
    void insert(String keyword, Integer userId);

    /**
     * 查询搜索记录
     *
     * @return
     */
    ResponseResult findUserSearch();

    /**
     * 删除历史记录
     *
     * @return
     */

    ResponseResult deleteUserSearch(HistorySearchDto dto);
}
