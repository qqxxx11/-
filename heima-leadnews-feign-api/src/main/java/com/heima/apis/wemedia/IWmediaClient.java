package com.heima.apis.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("leadnews-wemedia")
public interface IWmediaClient {

    @PostMapping("/api/v1/wemedia/save")
    public ResponseResult save(@RequestBody WmUser wmUser);

    @GetMapping("api/v1/channel/list")
    ResponseResult getChannels();

}

