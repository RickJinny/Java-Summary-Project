package com.java.rickjinny.server.service.uservip;
import com.java.rickjinny.model.entity.UserVip;
import com.java.rickjinny.model.mapper.UserVipMapper;
import com.java.rickjinny.server.enums.Constant;
import org.joda.time.DateTime;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 用户会员服务
 **/
@Service
public class UserVipService {

    private static final Logger log= LoggerFactory.getLogger(UserVipService.class);

    @Autowired
    private UserVipMapper vipMapper;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private Environment env;

    // 充值会员 - redisson 的 MapCache
    @Transactional(rollbackFor = Exception.class)
    public void addVip(UserVip vip) throws Exception {
        vip.setVipTime(DateTime.now().toDate());
        int res = vipMapper.insertSelective(vip);
        if (res > 0) {
            //假设前端 vipDay=20 (20s后会员过期失效) : 第一次提醒ttl=vipDay-x ;第二次提醒ttl=vipDay

            //1、到期前n天的提醒  2、到期后的提醒
            RMapCache<String, Integer> rMapCache = redisson.getMapCache(Constant.RedissonUserVIPKey);

            // TODO:第一次提醒
            String key = vip.getId() + Constant.SplitCharUserVip + Constant.VipExpireFlg.First.getType();
            Long firstTTL = Long.valueOf(String.valueOf(vip.getVipDay() - Constant.x));
            if (firstTTL > 0) {
                rMapCache.put(key, vip.getId(), firstTTL, TimeUnit.SECONDS);
            }

            // TODO:第二次提醒
            key = vip.getId() + Constant.SplitCharUserVip + Constant.VipExpireFlg.End.getType();
            Long secondTTL = Long.valueOf(vip.getVipDay());
            rMapCache.put(key, vip.getId(), secondTTL, TimeUnit.SECONDS);
        }
    }

    //充值会员-redisson的延迟队列实现
    @Transactional(rollbackFor = Exception.class)
    public void addVipV2(UserVip vip) throws Exception{
        vip.setVipTime(DateTime.now().toDate());
        int res=vipMapper.insertSelective(vip);
        if (res>0){
            //TODO:充值成功(现实一般是需要走支付的..在这里以成功插入db为准) - 设置两个过期提醒时间，
            //TODO:一个是vipDay后的；一个是在到达vipDay前 x 的时间
            //TODO:如，vipDay=10天，x=2，即代表vip到期 前2天 提醒一次，vip到期时提醒一次，即
            //TODO:第一次提醒的时间点为：ttl=10-2=8，即距离现在开始的8天后进行第一次提醒；
            //TODO:第二次提醒的时间点是：ttl=10，即距离现在开始的10天后进行第二次提醒   -- 以此类推
            //TODO: (时间的话，建议转化为s；当然啦，具体业务具体设定即可)

            //TODO:基于redisson的延迟队列实现，重点就在于 ttl 的计算 (为了测试方便,在这里我们以 s 为单位)；
            //TODO:如果是多次提醒的话，需要做一点标记

            RBlockingQueue<String> blockingQueue=redisson.getBlockingQueue(Constant.RedissonUserVipQueue);
            RDelayedQueue<String> rDelayedQueue=redisson.getDelayedQueue(blockingQueue);

            //TODO:第一次提醒
            String value=vip.getId()+Constant.SplitCharUserVip+Constant.VipExpireFlg.First.getType();
            Long firstTTL=Long.valueOf(String.valueOf(vip.getVipDay()-Constant.x));
            if (firstTTL>0){
                rDelayedQueue.offer(value,firstTTL,TimeUnit.SECONDS);
            }

            //TODO:第二次提醒
            value=vip.getId()+Constant.SplitCharUserVip+Constant.VipExpireFlg.End.getType();
            Long secondTTL=Long.valueOf(vip.getVipDay());
            rDelayedQueue.offer(value,secondTTL,TimeUnit.SECONDS);
        }
    }
}





























