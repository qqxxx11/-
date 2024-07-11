package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.heima.apis.article.IArticleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    /**
     * 自媒体文章审核
     *
     * @param id
     */

    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private IArticleClient iArticleClient;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;

    @Override

    public void AutoScan(Integer id) {

        //1. 查询文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            //提取内容中的文本和图片
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);

//            //2. 审核文章内容 阿里云接口 注：因为个人用不了所有直接设置为默认通过
//            boolean isTextScan = handleTextScan((String) textAndImages.get("content"), wmNews);
//            if (!isTextScan) return;//审核失败结束方法
//
//            //3. 审核图片内容 阿里云接口 注：因为个人用不了所有直接设置为默认通过
//            boolean isImagesScan = handleImagesScan((String) textAndImages.get("images"), wmNews);
//            if (!isImagesScan) return;//审核失败结束方法

            //4.审核成功保存文章到app端
            boolean isImagesScan = true;
            boolean isTextScan = true;
            if (isImagesScan == true && isTextScan == true) {
                ArticleDto dto = new ArticleDto();
                //属性拷贝
                BeanUtils.copyProperties(wmNews, dto);
                dto.setLayout(wmNews.getType());
                //频道
                WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
                if (wmChannel != null) {
                    dto.setChannelName(wmChannel.getName());
                }

                //作者
                dto.setAuthorId(Long.valueOf(wmNews.getUserId()));
                WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
                if (wmUser != null) {
                    dto.setAuthorName(wmUser.getName());
                }
                //设置文章id，有id则是修改没有则是保存
                if (wmNews.getArticleId() != null) {
                    dto.setId(wmNews.getArticleId());
                }
                dto.setCreatedTime(new Date());

                ResponseResult responseResult = iArticleClient.saveArticle(dto);
                if (!responseResult.getCode().equals(200)) {
                    throw new RuntimeException("保存文章失败");
                }
                wmNews.setArticleId((Long) responseResult.getData());
                wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());
                wmNews.setReason("审核成功");
                wmNewsMapper.updateById(wmNews);
            }

        }
    }

    /**
     * 图片审核
     *
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleImagesScan(String images, WmNews wmNews) {
        return true;
    }

    /**
     * 文本内容审核
     *
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        return true;
    }

    /**
     * 提取内容中的文本和图片和封面图片
     *
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        //存储文本内
        StringBuilder stringBuilder = new StringBuilder();
        //存储图片内容
        List<String> images = new ArrayList<>();

        //内容中的文本和图片
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")) {
                    stringBuilder.append(map.get("value"));
                }
                if (map.get("type").equals("image")) {
                    images.add((String) map.get("value"));
                }
            }
        }

        //封面图片
        if (StringUtils.isNotBlank(wmNews.getImages())) {
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("content", stringBuilder.toString());
        map.put("images", images);

        return map;
    }
}
