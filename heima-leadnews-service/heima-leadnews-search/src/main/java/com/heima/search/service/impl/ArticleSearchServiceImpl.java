package com.heima.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dto.UserSearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.service.ApUserSearchService;
import com.heima.search.service.ArticleSearchService;
import com.heima.utils.thread.ApThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ArticleSearchServiceImpl implements ArticleSearchService {
    /**
     * es文章搜索
     *
     * @param dto
     * @return
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private ApUserSearchService apUserSearchService;

    @Override
    public ResponseResult search(UserSearchDto dto) throws IOException {
        //1.检查参数
        if (dto == null || !StringUtils.isNotBlank(dto.getSearchWords())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //异步调用保存搜索记录

        ApUser user = ApThreadLocalUtil.getUser();
        if (user != null && dto.getFromIndex() == 0) {
            apUserSearchService.insert(dto.getSearchWords(), user.getId());
        }

        //2.设置查询条件
        SearchRequest searchRequest = new SearchRequest("app_info_article"); //设置查询库
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设置查询条件构造器
        //布尔查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //关键词分词之后查询  ---构建一个既可以查询title有可以查询content的查询器再放入boolQuery中
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders //一个既可以查询title有可以查询content的查询器
                .queryStringQuery(dto.getSearchWords())
                .field("title")
                .field("content")
                .defaultOperator(Operator.OR);
        boolQuery.must(queryStringQueryBuilder);//放入boolQuery中

        //查询小于mindate的数据
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders  //范围查询根据发布时间
                .rangeQuery("publishTime")
                .lt(dto.getMinBehotTime().getTime());
        boolQuery.filter(rangeQueryBuilder); //加入布尔查询中

        //分页查询
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(dto.getPageSize());

        //安装发布时间倒序查询
        searchSourceBuilder.sort("publishTime", SortOrder.DESC);

        //设置高亮 title
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder
                .field("title")
                .preTags("<font style='color: red; font-size: inherit;'>")
                .postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);


        searchSourceBuilder.query(boolQuery);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //返回结果
        //封装结果
        List<Map> list = new ArrayList<>();
        SearchHit[] searchHits = response.getHits().getHits();//获取每一条文章的标题高亮数据
        for (SearchHit hit : searchHits) {//获取其中一条的标题高亮数据
            String json = hit.getSourceAsString();
            Map map = JSON.parseObject(json, Map.class);//json格式数据转为map

            //处理高亮
            if (hit.getHighlightFields() != null && !hit.getHighlightFields().isEmpty()) {
                //存在高亮数据
                Text[] titles = hit.getHighlightFields().get("title").getFragments();
                String title = StringUtils.join(titles);
                //高亮标题
                map.put("h title", title);
            } else {
                //原始标题
                map.put("h title", map.get("title"));
            }
            list.add(map);
        }
        return ResponseResult.okResult(list);
    }
}
