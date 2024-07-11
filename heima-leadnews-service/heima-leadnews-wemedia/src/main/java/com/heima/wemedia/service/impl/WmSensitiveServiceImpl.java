package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.SensitiveDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class WmSensitiveServiceImpl extends ServiceImpl<WmSensitiveMapper, WmSensitive> implements WmSensitiveService {


    /**
     * 条件查询敏感词列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult getListByDto(SensitiveDto dto) {
        //检查参数
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.checkParam();
        //设置查询条件
        LambdaQueryWrapper<WmSensitive> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //倒叙
        lambdaQueryWrapper.orderByDesc(WmSensitive::getCreatedTime);
        //按照名字模糊查询
        if (StringUtils.isNotBlank(dto.getName())) {
            lambdaQueryWrapper.like(WmSensitive::getSensitives, dto.getName());
        }
        //设置分页
        IPage page = new Page(dto.getPage(), dto.getSize());
        page = page(page, lambdaQueryWrapper);
        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }

    /**
     * 新增敏感词
     *
     * @param adSensitive
     * @return
     */
    @Override
    public ResponseResult insert(WmSensitive adSensitive) {
        //检查参数
        if (adSensitive == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //判断是否存在
        WmSensitive sensitive = getOne(Wrappers.<WmSensitive>lambdaQuery()
                .eq(WmSensitive::getSensitives, adSensitive.getSensitives()));
        if (sensitive != null) {
            ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "敏感词已经存在");
        }

        //保存
        adSensitive.setCreatedTime(new Date());
        save(adSensitive);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
