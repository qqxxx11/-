package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private WmNewsService wmNewsService;

    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmNewsPageReqDto dto) {
        return wmNewsService.findList(dto);
    }

    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto) {
        return wmNewsService.submitNews(dto);
    }

    /**
     * 查看详情
     *
     * @param id
     * @return
     */
    @GetMapping("/one/{id}")
    public ResponseResult one(@PathVariable int id) {
        return wmNewsService.one(id);
    }

    /**
     * 删除文章
     *
     * @param id
     * @return
     */
    @GetMapping("/del_news/{id}")
    public ResponseResult deleteNews(@PathVariable int id) {
        return wmNewsService.delete(id);
    }

    /**
     * 文章上下架
     *
     * @param dto
     * @return
     */
    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDto dto) {
        return wmNewsService.downOrUp(dto);
    }
}
