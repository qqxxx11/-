package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有频道
     *
     * @return
     */
    public ResponseResult findAll();

    /**
     * 添加频道
     *
     * @param adChannel
     * @return
     */
    ResponseResult insert(WmChannel adChannel);

    /**
     * 频道名称分页查询
     *
     * @param dto
     * @return
     */
    ResponseResult findByNamePage(ChannelDto dto);

    /**
     * 修改频道
     *
     * @param adChannel
     * @return
     */
    ResponseResult updateByAd(WmChannel adChannel);

    /**
     * 删除频道
     *
     * @param id
     * @return
     */
    ResponseResult deleteById(Integer id);
}