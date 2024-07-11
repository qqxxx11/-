package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.heima.apis.wemedia.IWmediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.HotArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vo.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotArticleServiceImpl implements HotArticleService {
    @Autowired
    HotArticleMapper hotArticleMapper;
    @Autowired
    ApArticleMapper apArticleMapper;
    @Autowired
    IWmediaClient iWmediaClient;
    @Autowired
    CacheService cacheService;

    /**
     * 计算热点文章
     */
    @Override
    public void computeHotArticle() {
        Date dateParam = DateTime.now().minusDays(5).toDate();
        //1. 查询前5天的文章数据
        List<ApArticle> articleList = apArticleMapper.findArticleListBy5days(dateParam);
        //2. 计算文章分值
        List<HotArticleVo> hotArticleVoList = new ArrayList<>();

        if (!articleList.isEmpty()) {
            for (ApArticle apArticle : articleList) {
                HotArticleVo hot = new HotArticleVo();
                BeanUtils.copyProperties(apArticle, hot);
                //计算分值
                Integer score = computeScore(apArticle);
                hot.setScore(score);
                hotArticleVoList.add(hot);
            }
        }

        //3。 缓存分值最高的30条数据
        //为所有频道缓存30条热点文章
        //获取所有频道
        ResponseResult response = iWmediaClient.getChannels();
        if (response.getCode().equals(200)) {
            String json = JSON.toJSONString(response.getData().toString());
            List<WmChannel> wmChannelList = JSONArray.parseArray(json, WmChannel.class);
            for (WmChannel wmChannel : wmChannelList) {
                List<HotArticleVo> hotArticleVos = hotArticleVoList.stream()
                        .filter(x -> x.getChannelId().equals(wmChannel.getId()))//过滤出文章热点文章集合中在当前评到的文章
                        .collect(Collectors.toList())//转为list<HotArticleVo>集合
                        .stream()
                        .sorted(Comparator.comparing(HotArticleVo::getScore).reversed())//排序，倒序
                        .collect(Collectors.toList());//转为集合
                //截取前30条
                if (hotArticleVos.size() > 30) {
                    hotArticleVos = hotArticleVos.subList(0, 30);
                }
                //存入redis
                cacheService.set(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + wmChannel.getId(), JSON.toJSONString(hotArticleVos));
            }
        }
        //设置推荐数据
        //给文章进行排序，取30条分值较高的文章存入redis，key：频道id value：30条文章数据
        hotArticleVoList = hotArticleVoList.stream()
                .sorted(Comparator.comparing(HotArticleVo::getScore).reversed())//排序，倒序
                .collect(Collectors.toList());//转为集合
        if (hotArticleVoList.size() > 30) {
            hotArticleVoList = hotArticleVoList.subList(0, 30);
        }
        //存入redis
        cacheService.set(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG, JSON.toJSONString(hotArticleVoList));

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
