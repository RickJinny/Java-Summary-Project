package com.java.rickjinny.server.service.uservip;

import com.java.rickjinny.model.entity.UserVip;
import com.java.rickjinny.model.mapper.UserVipMapper;
import com.java.rickjinny.server.enums.Constant;
import com.java.rickjinny.server.service.mail.MailService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryExpiredListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class RedissonMapCacheUserVip implements ApplicationRunner,Ordered{

    private static final Logger logger= LoggerFactory.getLogger(RedissonMapCacheUserVip.class);

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private UserVipMapper vipMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private Environment env;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        logger.info("----不间断的执行一些自定义的操作-order 1");
        this.listenUserVip();
    }

    @Override
    public int getOrder() {
        return 1;
    }

    /**
     * 监听用户会员过期的数据 - 1、到期前n天的提醒  2、到期后的提醒 - 需要给相应用户发送通知(邮件)，
     * 告知会员即将过期或者已经过期的相关信息
     */
    private void listenUserVip(){
        RMapCache<String, Integer> rMapCache = redisson.getMapCache(Constant.RedissonUserVIPKey);
        rMapCache.addListener(new EntryExpiredListener<String,Integer>() {
            @Override
            public void onExpired(EntryEvent event) {
                // key = 充值记录id - 类型
                String key = String.valueOf(event.getKey());
                // value = 充值记录id
                String value = String.valueOf(event.getValue());
                logger.info("--监听用户会员过期的数据，监听到数据：key={}  value={}", key, value);
                if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
                    String[] arr = StringUtils.split(key, Constant.SplitCharUserVip);
                    Integer id = Integer.valueOf(value);
                    UserVip vip = vipMapper.selectByPrimaryKey(id);
                    if (vip != null && 1 == vip.getIsActive() && StringUtils.isNotBlank(vip.getEmail())) {
                        // 区分是第一次的提醒还是最后一次的提醒
                        Integer type = Integer.valueOf(arr[1]);
                        if (Constant.VipExpireFlg.First.getType().equals(type)) {
                            String content = String.format(env.getProperty("vip.expire.first.content"), vip.getPhone());
                            mailService.sendSimpleEmail(env.getProperty("vip.expire.first.subject"), content, vip.getEmail());
                        } else {
                            int res = vipMapper.updateExpireVip(id);
                            if (res > 0) {
                                String content = String.format(env.getProperty("vip.expire.end.content"), vip.getPhone());
                                mailService.sendSimpleEmail(env.getProperty("vip.expire.end.subject"), content, vip.getEmail());
                            }
                        }
                    }
                }
            }
        });
    }
}



























