package com.heima.user.service.impl;

import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.service.ApUserRelationService;
import com.heima.utils.thread.ApThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApUserRelationServiceImpl implements ApUserRelationService {
    @Autowired
    private CacheService cacheService;

    /**
     * 关注/取消关注
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult follow(UserRelationDto dto) {
        //判断参数有效性
        if (dto == null) {
            ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //判断用户是否登录
        ApUser apUser = ApThreadLocalUtil.getUser();
        if (apUser == null) {
            ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Integer userId = apUser.getId();
        Integer followId = dto.getAuthorId();

        //关注或者取消关注
        if (dto.getOperation() == 0) {
            //关注
            //将对方写入我的关注    APUSER-FOLLOW-用户id ： 文章作者id
            cacheService.zAdd(BehaviorConstants.APUSER_FOLLOW_RELATION + userId, followId.toString(), System.currentTimeMillis());
            //将我写入对方粉丝      APUSER-FANS- 文章作者id ： 用户id
            cacheService.zAdd(BehaviorConstants.APUSER_FANS_RELATION + followId, userId.toString(), System.currentTimeMillis());

        } else if (dto.getOperation() == 1) {
            //取消关注
            cacheService.zRemove(BehaviorConstants.APUSER_FOLLOW_RELATION + userId, followId.toString());
            cacheService.zRemove(BehaviorConstants.APUSER_FANS_RELATION + followId, userId.toString());
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
