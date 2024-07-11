package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.SensitiveDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.service.WmSensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/sensitive")
public class WmSensitiveController {

    @Autowired
    private WmSensitiveService wmSensitiveService;

    /**
     * 删除敏感词
     *
     * @param id
     * @return
     */
    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable("id") Integer id) {
        wmSensitiveService.removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 展示敏感词列表
     *
     * @param dto
     * @return
     */

    @PostMapping("/list")
    public ResponseResult list(@RequestBody SensitiveDto dto) {
        return wmSensitiveService.getListByDto(dto);
    }

    /**
     * 新增敏感词
     *
     * @param adSensitive
     * @return
     */

    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmSensitive adSensitive) {
        return wmSensitiveService.insert(adSensitive);
    }

    /**
     * 修改
     * @param adSensitive
     * @return
     */

    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmSensitive adSensitive) {
        if (adSensitive == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        wmSensitiveService.updateById(adSensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
