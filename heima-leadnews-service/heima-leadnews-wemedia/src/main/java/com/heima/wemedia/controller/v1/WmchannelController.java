package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.ChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
public class WmchannelController {

    @Autowired
    private WmChannelService wmChannelService;

    /**
     * 查询所有频道
     *
     * @return
     */
    @GetMapping("/channels")
    public ResponseResult findAll() {
        return wmChannelService.findAll();
    }

    /**
     * 保存频道
     *
     * @param adChannel
     * @return
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmChannel adChannel) {
        return wmChannelService.insert(adChannel);
    }

    /**
     * 根据频道名模糊查询列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult list(ChannelDto dto) {
        return wmChannelService.findByNamePage(dto);
    }

    /**
     * 修改频道
     *
     * @param adChannel
     * @return
     */
    @PostMapping("/update")
    public ResponseResult update(WmChannel adChannel) {
        return wmChannelService.updateByAd(adChannel);
    }

    /**
     * 删除频道
     *
     * @param id
     * @return
     */

    @GetMapping("/del/{id}")
    public ResponseResult delete(@PathVariable Integer id) {
        return wmChannelService.deleteById(id);
    }

}
