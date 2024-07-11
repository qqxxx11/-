package com.heima.user.controller.v1;

import com.heima.common.constants.UserConstants;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;
import com.heima.user.service.ApUserRealNameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class ApUserRealNameController {

    @Autowired
    private ApUserRealNameService apUserRealNameService;

    /**
     * 查询列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody AuthDto dto) {
        return apUserRealNameService.listByDto(dto);
    }

    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody AuthDto dto ){
        return apUserRealNameService.updateStatus(dto, UserConstants.PASS_AUTH);
    }

    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody AuthDto dto ){
        return apUserRealNameService.updateStatus(dto, UserConstants.FAIL_AUTH);
    }


}
