package com.java.rickjinny.server.service.message;

import com.java.rickjinny.model.mapper.SendRecordMapper;
import com.java.rickjinny.server.common.CommonService;
import com.java.rickjinny.server.enums.Constant;
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

/**
 * 短信验证码失效验证 - key（其中的元素 - field）失效监听
 */
public class RedissonMapCacheMessageCode implements ApplicationRunner, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RedissonMapCacheMessageCode.class);

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SendRecordMapper recordMapper;

    @Autowired
    private CommonService commonService;

    /**
     * 应用在启动以及运行期间，可以不间断的执行一些我们自定义的服务逻辑
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("----应用在启动以及运行期间，可以不间断的执行一些我们自定义的服务逻辑-order 0--");
        listenerExpireCode();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 监听器：监听 mapCache 里过期失效的验证码
     */
    private void listenerExpireCode() {
        try {
            RMapCache<String, String> rMapCache = redissonClient.getMapCache(Constant.RedissonMsgCodeKey);
            rMapCache.addListener(new EntryExpiredListener<String, String>() {

                @Override
                public void onExpired(EntryEvent<String, String> entryEvent) {
                    try {
                        String phone = entryEvent.getKey();
                        String msgCode = entryEvent.getValue();
                        logger.info("----当前手机号：{}，对应的验证码：{} ----即将失效", phone, msgCode);
                        if (StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(msgCode)) {
                            int res = recordMapper.updateExpirePhoneCode(phone, msgCode);
                            if (res > 0) {
                                commonService.recordLog(phone + "--" + msgCode,
                                        "监听mapCache里过期失效的验证码", "listenExpireCode");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
