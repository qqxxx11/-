package com.heima.article.stream;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.HotArticleConstants;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.mess.UpdateArticleMess;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class HotArticleStreamHandler {
    @Bean
    public KStream<String, String> KStream(StreamsBuilder streamsBuilder) {
        //接收消息
        KStream<String, String> stream = streamsBuilder.stream(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC);
        //聚合处理
        stream
                //重置消息的key和value
                .map((key, value) -> {
                    UpdateArticleMess mess = JSON.parseObject(value, UpdateArticleMess.class);
                    return new KeyValue<>(mess.getArticleId().toString(), mess.getType().name() + ":" + mess.getAdd());
                })
                //根据id聚合
                .groupBy((key, value) -> key)
                //时间窗口，当前设定时间内聚合消息处理之后发送
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                /**
                 * 自行完成聚合计算
                 */
                .aggregate(new Initializer<String>() {
                               /**
                                * 初始方法
                                * @return 返回值是消息的value
                                */
                               @Override
                               public String apply() {
                                   //阅读，点赞，收藏
                                   return "COLLECTION:0,COMMENT:0,LIKES:0,VIEWS:0";
                               }
                           },
                        new Aggregator<String, String, String>() {
                            /**
                             * 真正的聚合操作
                             * @param key
                             * @param value
                             * @param aggValue  初始化的内容
                             * @return
                             */
                            @Override
                            public String apply(String key, String value, String aggValue) {
                                if (StringUtils.isBlank(value)){
                                    return aggValue;
                                }
                                //获取初始值，也就是当前的时间窗口内聚合计算之后的值
                                String[] aggAry = aggValue.split(",");
                                int col = 0,com=0,lik=0,vie=0;
                                for (String agg : aggAry) {
                                    String[] split = agg.split(":");
                                    switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])){
                                        case LIKES:
                                            lik=Integer.parseInt(split[1]);
                                            break;
                                        case COLLECTION:
                                            col=Integer.parseInt(split[1]);
                                            break;
                                        case COMMENT:
                                            com=Integer.parseInt(split[1]);
                                            break;
                                        case VIEWS:
                                            vie=Integer.parseInt(split[1]);
                                            break;
                                    }
                                }
                                //聚合累加操作
                                String[] valAry = value.split(":");
                                switch (UpdateArticleMess.UpdateArticleType.valueOf(valAry[0])){
                                    case LIKES:
                                        lik+=Integer.parseInt(valAry[1]);
                                        break;
                                    case COLLECTION:
                                        col+=Integer.parseInt(valAry[1]);
                                        break;
                                    case COMMENT:
                                        com+=Integer.parseInt(valAry[1]);
                                        break;
                                    case VIEWS:
                                        vie+=Integer.parseInt(valAry[1]);
                                        break;
                                }
                                return String.format("COLLECTION:%d,COMMENT:%d,LIKES:%d,VIEWS:%d",col,com,lik,vie);
                            }
                        }, Materialized.as("hot-article-stream-count-001"))
                .toStream()
                .map((key,value)-> new KeyValue<>(key.key(),formatObj(key.key(),value)))
                //发送消息
                .to(HotArticleConstants.HOT_ARTICLE_INCR_HANDLE_TOPIC);
        return stream;

    }
    /**
     * 格式化消息的value数据
     * @param articleId
     * @param value
     * @return
     */
    public String formatObj(String articleId,String value){
        ArticleVisitStreamMess mess = new ArticleVisitStreamMess();
        mess.setArticleId(Long.valueOf(articleId));
        //COLLECTION:0,COMMENT:0,LIKES:0,VIEWS:0
        String[] valAry = value.split(",");
        for (String val : valAry) {
            String[] split = val.split(":");
            switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])){
                case COLLECTION:
                    mess.setCollect(Integer.parseInt(split[1]));
                    break;
                case COMMENT:
                    mess.setComment(Integer.parseInt(split[1]));
                    break;
                case LIKES:
                    mess.setLike(Integer.parseInt(split[1]));
                    break;
                case VIEWS:
                    mess.setView(Integer.parseInt(split[1]));
                    break;
            }
        }
        return JSON.toJSONString(mess);

    }
}
