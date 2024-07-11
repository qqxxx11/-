package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {

    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmNewsMapper wmNewsMapper;

    /**
     * 查询所有频道
     *
     * @return
     */
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }

    /**
     * 添加频道
     *
     * @param adChannel
     * @return
     */
    @Override
    public ResponseResult insert(WmChannel adChannel) {
        if (adChannel == null || adChannel.getName() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //判断是否重复
        WmChannel wmChannel = getOne(Wrappers.<WmChannel>lambdaQuery().eq(WmChannel::getName, adChannel.getName()));
        if (wmChannel != null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "重复的频道");
        }
        adChannel.setCreatedTime(new Date());
        save(adChannel);
        return null;
    }

    /**
     * 频道名称分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findByNamePage(ChannelDto dto) {
        //检查参数
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //检查分页
        dto.checkParam();
        //模糊查询+分页
        IPage page = new Page(dto.getPage(), dto.getSize());
        //设置模糊查询
        LambdaQueryWrapper<WmChannel> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (dto.getName() != null) {
            lambdaQueryWrapper.like(WmChannel::getName, dto.getName());
        }

        page = page(page, lambdaQueryWrapper);
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    /**
     * 删除频道
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult deleteById(Integer id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //判断是否启用
        WmChannel wmChannel = wmChannelMapper.selectById(id);
        if (wmChannel.getStatus()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "频道启用不能删除");
        }
        //删除
        removeById(id);
        return null;
    }

    /**
     * 修改频道
     *
     * @param adChannel
     * @return
     */
    @Override
    public ResponseResult updateByAd(WmChannel adChannel) {
        if (adChannel == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //判断是否被引用
        Integer count = wmNewsMapper.selectCount(Wrappers.<WmNews>lambdaQuery()
                .eq(WmNews::getChannelId, adChannel.getId())
                .eq(WmNews::getStatus, WmNews.Status.PUBLISHED));
        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "频道被引用不能被修改和禁用");
        }
        //修改r
        updateByAd(adChannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}