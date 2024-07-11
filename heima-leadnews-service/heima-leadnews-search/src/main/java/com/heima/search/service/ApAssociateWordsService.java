package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dto.UserSearchDto;

public interface ApAssociateWordsService {
    /**
     * 搜索联想词
     *
     * @param dto
     * @return
     */
    ResponseResult search(UserSearchDto dto);

}
