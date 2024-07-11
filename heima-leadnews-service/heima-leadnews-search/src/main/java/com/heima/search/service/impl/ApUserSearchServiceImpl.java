package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.dtos.HistorySearchDto;
import com.heima.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.thread.ApThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ApUserSearchServiceImpl implements ApUserSearchService {
    /**
     * 保存用户搜索记录
     *
     * @param keyword 搜索关键词
     * @param userId  用户ID
     */
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    @Async
    public void insert(String keyword, Integer userId) {
        //1. 查询用户搜索关键词
        Query query = Query.query(Criteria
                .where("userId")
                .is(userId).and("keyword")
                .is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);
        //存在，更新到最新时间
        if (apUserSearch != null) {
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
        } else {
            apUserSearch = new ApUserSearch();
            apUserSearch.setUserId(userId);
            apUserSearch.setKeyword(keyword);
            apUserSearch.setCreatedTime(new Date());
            //不存在，判断总数据量是否超过10
            Query query1 = Query.query(Criteria
                            .where("userId")
                            .is(userId))
                    .with(Sort.by(Sort.Direction.DESC, "createdTime"));
            List<ApUserSearch> apUserSearchList = mongoTemplate.find(query1, ApUserSearch.class);
            if (apUserSearchList.size() < 10) {
                //没超过，保存关键字
                mongoTemplate.save(apUserSearch);
            } else {
                //超过，替换最后一条数据
                ApUserSearch lastUserSearch = apUserSearchList.get(apUserSearchList.size() - 1);
                mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(lastUserSearch.getId())), apUserSearch);
            }
        }
    }

    /**
     * 查询搜索记录
     *
     * @return
     */
    @Override
    public ResponseResult findUserSearch() {
        ApUser user = ApThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        List<ApUserSearch> userSearchList = mongoTemplate.find(Query.query(Criteria.where("userId").is(user.getId())).with(Sort.by(Sort.Direction.DESC, "createdTime")), ApUserSearch.class);
        return ResponseResult.okResult(userSearchList);
    }

    /**
     * 删除历史记录
     *
     * @return
     */
    @Override
    public ResponseResult deleteUserSearch(HistorySearchDto dto) {
        if (dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser user = ApThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        mongoTemplate.remove(Query.query(Criteria
                .where("userId")
                .is(user.getId())
                .and("id")
                .is(dto.getId())), ApUserSearch.class);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


}
