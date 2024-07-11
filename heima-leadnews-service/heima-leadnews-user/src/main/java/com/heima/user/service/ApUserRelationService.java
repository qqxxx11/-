package com.heima.user.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;

public interface ApUserRelationService {
    /**
     * 关注/取消关注
     *
     * @param dto
     * @return
     */
    ResponseResult follow(UserRelationDto dto);
}
