package com.heima.wemedia.feign;

import com.heima.apis.wemedia.IWmediaClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WmediaClient implements IWmediaClient {

    @Autowired
    private WmUserService wmUserService;
    @Autowired
    private WmChannelService wmChannelService;

    /**
     * 新增用户
     *
     * @param wmUser
     * @return
     */
    @Override
    @PostMapping("/api/v1/wemedia/save")
    public ResponseResult save(WmUser wmUser) {
        wmUserService.save(wmUser);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    @GetMapping("api/v1/channel/list")
    public ResponseResult getChannels() {

        return wmChannelService.findAll();
    }


}
