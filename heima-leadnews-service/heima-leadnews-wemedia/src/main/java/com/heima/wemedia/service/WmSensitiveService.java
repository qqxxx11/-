package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.SensitiveDto;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {

    /**
     * 条件查询敏感词列表
     * @param dto
     * @return
     */
    ResponseResult getListByDto(SensitiveDto dto);

    /**
     * 新增敏感词
     * @param adSensitive
     * @return
     */

    ResponseResult insert(WmSensitive adSensitive);
}
