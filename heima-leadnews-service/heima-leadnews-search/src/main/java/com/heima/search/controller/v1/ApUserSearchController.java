package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.search.dtos.HistorySearchDto;
import com.heima.search.service.ApUserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history")
public class ApUserSearchController {
    @Autowired
    private ApUserSearchService apUserSearchService;

    /**
     * 查询用户搜索记录
     *
     * @return
     */

    @PostMapping("/load")
    public ResponseResult findUserSearch() {
        return apUserSearchService.findUserSearch();
    }

    /**
     * 删除历史记录
     *
     * @param dto
     * @return
     */
    @PostMapping("/del")
    public ResponseResult delete(HistorySearchDto dto) {
        return apUserSearchService.deleteUserSearch(dto);
    }
}
