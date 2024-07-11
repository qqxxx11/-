package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;
import com.heima.model.user.pojos.ApUserRealname;

public interface ApUserRealNameService extends IService<ApUserRealname> {
    /**
     * 条件查询列表
     *
     * @param dto
     * @return
     */
    ResponseResult listByDto(AuthDto dto);
    ResponseResult updateStatus(AuthDto dto, Short status);
}
