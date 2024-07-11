package com.heima.article.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.vo.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.ApThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApArticleContentMapper articleContentMapper;
    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;
    @Autowired
    private CacheService cacheService;
    private final static short MAX_PAGE_SIZE = 50;

    /**
     * 加载文章列表
     *
     * @param dto
     * @param type 1 加载更多   2 加载最新
     * @return
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
        //1.检验参数
        //分页条数的校验
        Integer size = dto.getSize();
        if (size == null || size == 0) {
            size = 10;
        }
        //分页的值不超过50
        size = Math.min(size, MAX_PAGE_SIZE);


        //校验参数  -->type
        if (!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE) && !type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)) {
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        //频道参数校验
        if (StringUtils.isBlank(dto.getTag())) {
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        //时间校验
        if (dto.getMaxBehotTime() == null) dto.setMaxBehotTime(new Date());
        if (dto.getMinBehotTime() == null) dto.setMinBehotTime(new Date());

        //2.查询
        List<ApArticle> articleList = apArticleMapper.loadArticleList(dto, type);
        //3.结果返回
        return ResponseResult.okResult(articleList);
    }

    /**
     * 文章添加
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {

        //1. 检查参数
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);
        if (dto.getId() == null) {
            //2.1 不存在id 保存 文章 文章配置 文章内容
            //保存文章
            save(apArticle);
            //保存配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);
            //保存文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            articleContentMapper.insert(apArticleContent);
        } else {
            //2.2 存在id   保存 文章 文章内容
            //修改文章
            updateById(apArticle);
            //修改文章内容
/*
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
*/
            ApArticleContent apArticleContent = articleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, apArticle.getId()));
            articleContentMapper.insert(apArticleContent);
            /*           articleContentMapper.update(apArticleContent,
                    Wrappers.<ApArticleContent>lambdaUpdate()
                            .eq(ApArticleContent::getArticleId, apArticleContent.getArticleId())
                            .set(ApArticleContent::getContent, apArticleContent.getContent()));
*/
        }

        //异步调用 生成静态文件保存再minio
        articleFreemarkerService.buildArticleToMinIo(apArticle, dto.getContent());


        return ResponseResult.okResult(apArticle.getId());
    }

    /**
     * 用户行为数据回显
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult loadArticleBehavior(ArticleInfoDto dto) {
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser user = ApThreadLocalUtil.getUser();
        boolean isfollow = false, islike = false, isunlike = false, iscollection = false;
        if (user != null) {
            //是否关注
            Double score = cacheService.zScore(BehaviorConstants.APUSER_FOLLOW_RELATION + user.getId(), dto.getArticleId().toString());
            if (score != null) {
                isfollow = true;
            }
            //是否喜欢
            String likeBehaviorJson = (String) cacheService.hGet(BehaviorConstants.LIKE_BEHAVIOR + dto.getArticleId(), user.getId().toString());
            if (StringUtils.isNotBlank(likeBehaviorJson)) {
                islike = true;
            }
            //是否不喜欢
            Object o = cacheService.hGet(BehaviorConstants.UN_LIKE_BEHAVIOR + dto.getArticleId(), user.getId().toString());
            if (o != null) {
                isunlike = true;
            }
            //是否收藏
            Object o1 = cacheService.hGet(BehaviorConstants.COLLECTION_BEHAVIOR + user.getId(), dto.getArticleId().toString());
            if (o1 != null) {
                iscollection = true;
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isfollow", isfollow);
        resultMap.put("islike", islike);
        resultMap.put("isunlike", isunlike);
        resultMap.put("iscollection", iscollection);

        return ResponseResult.okResult(resultMap);
    }

    /**
     * 加载热点数据
     *
     * @param dto
     * @param type      1 加载更多 2加载最新
     * @param firstPage 是否为首页
     * @return
     */
    @Override
    public ResponseResult load2(ArticleHomeDto dto, Short type, boolean firstPage) {
        //如果为首页则在redis中加载热点数据
        if (firstPage) {
            String json = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + dto.getTag());
            if (StringUtils.isNotBlank(json)) {
                List<HotArticleVo> hotArticleVoList = JSONArray.parseArray(json, HotArticleVo.class);
                return ResponseResult.okResult(hotArticleVoList);
            }
        }
        //否则正常加载数据
        return load(dto, type);
    }

    /**
     * 更新文章分值 同时更新缓存中的热点文章数据
     *
     * @param mess
     */
    @Override
    public void updateScore(ArticleVisitStreamMess mess) {
        //更新文章的阅读，点赞，收藏，评论的数量
        ApArticle apArticle = getById(mess.getArticleId());

        apArticle.setCollection(apArticle.getCollection() == null ? 0 : apArticle.getCollection() + mess.getCollect());
        apArticle.setComment(apArticle.getComment() == null ? 0 : apArticle.getComment() + mess.getComment());
        apArticle.setLikes(apArticle.getLikes() == null ? 0 : apArticle.getLikes() + mess.getLike());
        apArticle.setViews(apArticle.getViews() == null ? 0 : apArticle.getViews() + mess.getView());

        updateById(apArticle);
        //计算分值
        Integer score = computeScore(apArticle) * 3;

        //替换当前文章对应的频道的热点数据----也就是纯在redis中的热点文章，如果分值小于当前实时的文章就替换
        String articleListStr = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId());
        replaceDateToRedis(articleListStr, apArticle, score, String.valueOf(apArticle.getChannelId()));
        //替换推荐对应的热点文章数据
        String articleListAllStr = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG);
        replaceDateToRedis(articleListAllStr, apArticle, score, ArticleConstants.DEFAULT_TAG);
    }


    /**
     * 替换热点文章数据
     *
     * @param articleListStr
     * @param apArticle
     * @param score
     * @param s
     */

    private void replaceDateToRedis(String articleListStr, ApArticle apArticle, Integer score, String s) {
        if (StringUtils.isNotBlank(articleListStr)) {
            List<HotArticleVo> hotArticleVoList = JSONArray.parseArray(articleListStr, HotArticleVo.class);
            boolean flag = true;
            //如果缓存中纯在文章只改变数值
            for (HotArticleVo hotarticleVo : hotArticleVoList) {
                if (hotarticleVo.getId() == apArticle.getId()) {
                    hotarticleVo.setScore(score);
                    flag = false;
                    break;
                }
            }
            //不存在文章则替换分值最小的文章，如果该频道下文章小于30条则直接存入该文章
            if (flag) {
                if (hotArticleVoList.size() >= 30) {
                    hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
                    HotArticleVo last = hotArticleVoList.get(hotArticleVoList.size() - 1);
                    if (last.getScore() < score) {
                        hotArticleVoList.remove(last);
                        HotArticleVo hotArticleVo = new HotArticleVo();
                        BeanUtils.copyProperties(apArticle, hotArticleVo);
                        hotArticleVo.setScore(score);
                        hotArticleVoList.add(hotArticleVo);
                    }
                } else {
                    HotArticleVo hotArticleVo = new HotArticleVo();
                    BeanUtils.copyProperties(apArticle, hotArticleVo);
                    hotArticleVo.setScore(score);
                    hotArticleVoList.add(hotArticleVo);
                }
                int[] num=new int[10];
                

            }

            //缓存到redis中
            hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
            cacheService.set(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + s, JSONArray.toJSONString(hotArticleVoList));
        }

    }

    /**
     * 计算热点文章分值
     *
     * @param apArticle
     * @return
     */

    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if (apArticle != null) {
            if (apArticle.getLikes() != null) {
                score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
            }
            if (apArticle.getViews() != null) {
                score += apArticle.getViews();
            }
            if (apArticle.getComment() != null) {
                score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
            }
            if (apArticle.getCollection() != null) {
                score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
            }
        }

        return score;
    }
}

