package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdUserService;
import com.heima.model.admin.dtos.AdUserDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdUserServiceImpl extends ServiceImpl<AdUserMapper, AdUser> implements AdUserService {
    /**
     * 登录
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(AdUserDto dto) {
        //判断参数有效性
        if (dto == null || dto.getPassword() == null || dto.getName() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //根据用户名查询用户消息
        AdUser user = getOne(Wrappers.<AdUser>lambdaQuery().eq(AdUser::getName, dto.getName()));
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户消息不存在");
        }
        //比对密码 -- 加盐
        String salt = user.getSalt();
        String password = dto.getPassword();
        String pswd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
        if (!pswd.equals(user.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
        //获取token
        String token = AppJwtUtil.getToken(user.getId().longValue());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        user.setSalt("");
        user.setPassword("");
        map.put("user", user);

        return ResponseResult.okResult(map);
    }
}
