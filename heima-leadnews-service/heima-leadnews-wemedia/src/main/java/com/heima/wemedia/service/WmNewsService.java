package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 条件查询文章列表
     *
     * @param dto
     * @return
     */
    public ResponseResult findList(WmNewsPageReqDto dto);

    /**
     * 发布修改文章或保存为草稿
     *
     * @param dto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto dto);

    /**
     * 查看详情
     *
     * @param id
     * @return
     */

    ResponseResult one(Integer id);

    /**
     * 删除文章
     *
     * @param id
     * @return
     */
    ResponseResult delete(Integer id);
    /**
     * 文章上下架
     *
     * @param dto
     * @return
     */
    ResponseResult downOrUp(WmNewsDto dto);
}
