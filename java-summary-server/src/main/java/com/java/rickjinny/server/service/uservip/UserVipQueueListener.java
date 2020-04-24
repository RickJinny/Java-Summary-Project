package com.java.rickjinny.server.service.uservip;/**
 * Created by Administrator on 2020/3/18.
 */

import com.java.rickjinny.model.entity.UserVip;
import com.java.rickjinny.model.mapper.UserVipMapper;
import com.java.rickjinny.server.enums.Constant;
import com.java.rickjinny.server.service.mail.MailService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 用户会员到期的提醒的监听
 * @Author:debug (SteadyJack)
 * @Link: weixin-> debug0868 qq-> 1948831260
 * @Date: 2020/3/18 21:26
 **/
@Component
public class UserVipQueueListener {

    private static final Logger log= LoggerFactory.getLogger(UserVipQueueListener.class);

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private UserVipMapper vipMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private Environment env;

    //近实时监听延迟队列中的待处理消息
    @Async("threadPoolTaskExecutor")
    @Scheduled(cron = "0/5 * * * * ?")
    public void manageExpireVip(){
        RBlockingQueue<String> blockingQueue=redisson.getBlockingQueue(Constant.RedissonUserVipQueue);
        if (blockingQueue!=null && !blockingQueue.isEmpty()){
            String element=blockingQueue.poll();

            if (StringUtils.isNotBlank(element)){
                log.info("近实时监听延迟队列中的待处理消息-监听到消息：{}",element);

                String[] arr= StringUtils.split(element,Constant.SplitCharUserVip);
                Integer id=Integer.valueOf(arr[0]);
                Integer type=Integer.valueOf(arr[1]);

                UserVip vip=vipMapper.selectByPrimaryKey(id);
                if (vip!=null && 1==vip.getIsActive() && StringUtils.isNotBlank(vip.getEmail())){
                    //区分是第一次的提醒还是最后一次的提醒
                    if (Constant.VipExpireFlg.First.getType().equals(type)){
                        String content=String.format(env.getProperty("vip.expire.first.content"),vip.getPhone());
                        mailService.sendSimpleEmail(env.getProperty("vip.expire.first.subject"),content,vip.getEmail());
                    }else{
                        int res=vipMapper.updateExpireVip(id);
                        if (res>0){
                            log.info("--近实时监听延迟队列中的待处理消-第二次提醒--");
                            String content=String.format(env.getProperty("vip.expire.end.content"),vip.getPhone());
                            mailService.sendSimpleEmail(env.getProperty("vip.expire.end.subject"),content,vip.getEmail());
                        }
                    }
                }
            }
        }

    }
}
































