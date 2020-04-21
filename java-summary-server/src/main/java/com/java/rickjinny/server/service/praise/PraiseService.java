package com.java.rickjinny.server.service.praise;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.java.rickjinny.model.dto.ArticlePraiseRankDto;
import com.java.rickjinny.model.dto.PraiseDto;
import com.java.rickjinny.model.entity.Article;
import com.java.rickjinny.model.entity.ArticlePraise;
import com.java.rickjinny.model.mapper.ArticleMapper;
import com.java.rickjinny.model.mapper.ArticlePraiseMapper;
import com.java.rickjinny.model.mapper.UserMapper;
import com.java.rickjinny.server.enums.Constant;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PraiseService {
    private static final Logger log= LoggerFactory.getLogger(PraiseService.class);

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticlePraiseMapper praiseMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String SplitChar="-";

    //获取文章的列表
    public List<Article> getAll() throws Exception{
        return articleMapper.selectAll();
    }

    //点赞文章
    @Transactional(rollbackFor = Exception.class)
    public Boolean praiseOn(PraiseDto dto) throws Exception{
        // 将基于 redis 的缓存判断 (redis的命令：setNX)
        final String recordKey = Constant.RedisArticlePraiseUser + dto.getUserId() + dto.getArticleId();
        // TODO: setIfAbsent 可以解决并发安全性问题(原子性操作)
        // TODO: 注意(当开启事务支持或者管道通信时，调用 setIfAbsent() 会返回 Null)
        Boolean canPraise = redisTemplate.opsForValue().setIfAbsent(recordKey, 1);
        if (canPraise) {
            // 将点赞的数据插入 DB
            ArticlePraise entity = new ArticlePraise(dto.getArticleId(), dto.getUserId(), DateTime.now().toDate());
            int res = praiseMapper.insertSelective(entity);
            if (res > 0) {
                // 叠加当前文章的点赞总数
                articleMapper.updatePraiseTotal(dto.getArticleId(), 1);
                // 缓存点赞的相关数据
                cachePraiseOn(dto);
            }
        }
        return true;
    }

    //缓存点赞相关的信息
    private void cachePraiseOn(final PraiseDto dto) throws Exception{
        // 选择的数据结构是Hash: Key - 字符串，存储至 redis 的标志符; Field -文章id ; Value -用户 id 列表
        HashOperations<String, String, Set<Integer>> praiseHash = redisTemplate.opsForHash();

        // 记录点赞过当前文章的用户 id 列表
        Set<Integer> uIds = praiseHash.get(Constant.RedisArticlePraiseHashKey, dto.getArticleId().toString());
        if (uIds == null || uIds.isEmpty()) {
            uIds = Sets.newHashSet();
        }
        // 把新的用户添加进去
        uIds.add(dto.getUserId());
        // 再把新的数据 put 进 HashOperations 中
        praiseHash.put(Constant.RedisArticlePraiseHashKey, dto.getArticleId().toString(), uIds);

        // 缓存点赞排行榜
        this.cachePraiseRank(dto, uIds.size());

        // 缓存用户的点赞轨迹(用户点赞过的历史文章)
        this.cacheUserPraiseArticle(dto,true);
    }

    //取消点赞
    @Transactional(rollbackFor = Exception.class)
    public Boolean praiseCancel(PraiseDto dto) throws Exception{
        final String recordKey = Constant.RedisArticlePraiseUser + dto.getUserId() + dto.getArticleId();
        // 查看当前用户是否已经点赞过当前文章：只有点赞过当前文章，才能取消点赞
        Boolean hasPraise = redisTemplate.hasKey(recordKey);
        if (hasPraise) {
            //移除掉db中的记录
            int res = praiseMapper.cancelPraise(dto.getArticleId(), dto.getUserId());
            if (res > 0) {
                //移除缓存中：用户的点赞文章记录
                redisTemplate.delete(recordKey);

                //更新文章的点赞总数
                articleMapper.updatePraiseTotal(dto.getArticleId(), -1);

                //缓存取消点赞相关的信息
                cachePraiseCancel(dto);
            }
        }
        return true;
    }

    //缓存取消点赞时的相关信息
    private void cachePraiseCancel(final PraiseDto dto) throws Exception{
        HashOperations<String, String, Set<Integer>> praiseHash = redisTemplate.opsForHash();
        //记录点赞过当前文章的用户id列表
        Set<Integer> uIds = praiseHash.get(Constant.RedisArticlePraiseHashKey, dto.getArticleId().toString());
        if (uIds != null && !uIds.isEmpty() && uIds.contains(dto.getUserId())) {
            //核心操作：就是将当前用户id 从 用户id列表中移除
            uIds.remove(dto.getUserId());
            praiseHash.put(Constant.RedisArticlePraiseHashKey, dto.getArticleId().toString(), uIds);
        }

        //缓存点赞排行榜
        this.cachePraiseRank(dto, uIds.size());

        //缓存用户的点赞记录(用户的维护)
        this.cacheUserPraiseArticle(dto, false);
    }

    //获取文章详情-点赞过的用户列表-排行榜
    public Map<String,Object> getArticleInfo(final Integer articleId, final Integer currUserId) throws Exception{
        Map<String,Object> resMap=Maps.newHashMap();

        //文章本身的信息
        resMap.put("articleInfo~文章详情",articleMapper.selectByPK(articleId));

        //获取点赞过当前文章的用户列表 ~ 需要获取用户的昵称/姓名 ~ 如果是多个的话，那么用 , 拼接 即可
        //Key-字符串，存储至redis的标志符; Field-文章id ;Value-用户id列表
        HashOperations<String,String,Set<Integer>> praiseHash=redisTemplate.opsForHash();
        Set<Integer> uIds=praiseHash.get(Constant.RedisArticlePraiseHashKey,articleId.toString());
        if (uIds!=null && !uIds.isEmpty()){
            resMap.put("userIds~用户id列表",uIds);

            String ids= Joiner.on(",").join(uIds);
            resMap.put("userNames~用户姓名列表",userMapper.selectNamesById(ids));

            //当前用户currUserId是否点赞过当前文章(其实就是 判断是否存在于集合set里面)
            if (currUserId!=null){
                resMap.put("currUserHasPraise-当前用户是否点赞文章",uIds.contains(currUserId));
            }
        }

        //获取点赞排行榜-根据点赞数的高低排行榜
        List<ArticlePraiseRankDto> ranks=Lists.newLinkedList();
        ZSetOperations<String,String> praiseSort=redisTemplate.opsForZSet();
        Long total=praiseSort.size(Constant.RedisArticlePraiseSortKey);
        Set<String> set=praiseSort.reverseRange(Constant.RedisArticlePraiseSortKey,0L,total);
        if (set!=null && !set.isEmpty()){
            set.forEach(value -> {
                Double score=praiseSort.score(Constant.RedisArticlePraiseSortKey,value);

                if (score>0){
                    //比如：2-Redis实战二-hash实战
                    Integer pos= StringUtils.indexOf(value,SplitChar);
                    if (pos>0){
                        String aId=StringUtils.substring(value,0,pos);
                        String aTitle=StringUtils.substring(value,pos+1);

                        ranks.add(new ArticlePraiseRankDto(aId,aTitle,score.toString(),score));
                    }
                }
            });
        }
        resMap.put("articlePraiseRanks~根据点赞数高低得到文章的排行榜",ranks);

        return resMap;
    }

    // 缓存点赞排行榜（SortedSet; ZSet）~ value=文章id-文章标题 ~ score=点赞总数 - 开发的逻辑需要适用于“点赞”与“取消点赞”
    private void cachePraiseRank(final PraiseDto dto,final Integer total){
        String value = dto.getArticleId() + SplitChar + dto.getTitle();
        ZSetOperations<String, String> praiseSort = redisTemplate.opsForZSet();
        // 为了增量更新，需要移除旧的值
        praiseSort.remove(Constant.RedisArticlePraiseSortKey, value);
        // 塞入文章-点赞数据
        praiseSort.add(Constant.RedisArticlePraiseSortKey, value, total.doubleValue());
    }


    /**
     * 以用户为维度，缓存用户点赞过的历史文章
     * key = 存储至 redis 的标志符;  field = 用户id - 文章id;  value = 文章标题
     *
     * @param dto
     * @param isOn true = 点赞, 直接存储;  false = 取消点赞, 重置 value 为 null / ""
     */
    private void cacheUserPraiseArticle(final PraiseDto dto, Boolean isOn) {
        final String field = dto.getUserId() + SplitChar + dto.getArticleId();
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        if (isOn) {
            hash.put(Constant.RedisArticleUserPraiseKey, field, dto.getTitle());
        } else {
            hash.put(Constant.RedisArticleUserPraiseKey, field, "");
        }
    }

    //以用户为维度~获取用户详情以及点赞过的历史文章
    public Map<String,Object> getUserArticles(final Integer currUserId) throws Exception{
        Map<String,Object> resMap= Maps.newHashMap();

        //用户的详情-直接查询db
        resMap.put("userInfo-用户详情",userMapper.selectByPrimaryKey(currUserId));

        //获取用户点赞过的历史文章
        List<PraiseDto> userPraiseArticles= Lists.newLinkedList();

        HashOperations<String,String,String> hash=redisTemplate.opsForHash();
        //field-value对
        Map<String,String> map=hash.entries(Constant.RedisArticleUserPraiseKey);
        for (Map.Entry<String,String> entry:map.entrySet()){
            //field=用户id - 文章id；value=文章标题
            String field=entry.getKey();
            String value=entry.getValue();

            String[] arr=StringUtils.split(field,SplitChar);

            //判断 value是否为空-如果为空，则代表用户已经取消点赞
            if (StringUtils.isNotBlank(value)){
                //筛选、过滤、检索出 当前用户id 的文章
                if (currUserId.toString().equals(arr[0])){
                    userPraiseArticles.add(new PraiseDto(currUserId,Integer.valueOf(arr[1]),value));
                }
            }
        }
        resMap.put("userPraiseArticles-用户点赞过的历史文章",userPraiseArticles);

        return resMap;
    }
}
