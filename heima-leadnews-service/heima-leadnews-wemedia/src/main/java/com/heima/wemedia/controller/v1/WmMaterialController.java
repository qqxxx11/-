package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        return wmMaterialService.uploadPicture(multipartFile);
    }

    /**
     * 显示所有素材
     *
     * @param dto
     * @return
     */

    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmMaterialDto dto) {
        return wmMaterialService.findList(dto);
    }

    /**
     * 删除图片
     *
     * @param id
     * @return
     */
    @GetMapping("/del_picture/{id}")
    public ResponseResult deletePicture(@PathVariable int id) {
        return wmMaterialService.deleteById(id);
    }

    /**
     * 取消收藏
     *
     * @param id
     * @return
     */
    @GetMapping("/cancel_collect/{id}")
    public ResponseResult cancelCollect(@PathVariable int id) {
        return wmMaterialService.cancelCollect(id);
    }

    /**
     * 收藏
     *
     * @param id
     * @return
     */
    @GetMapping("/collect/{id}")
    public ResponseResult collect(@PathVariable int id) {
        return wmMaterialService.collect(id);
    }


}
