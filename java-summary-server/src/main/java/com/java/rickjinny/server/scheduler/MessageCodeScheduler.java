package com.java.rickjinny.server.scheduler;/**
 * Created by Administrator on 2020/3/17.
 */

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.java.rickjinny.model.entity.SendRecord;
import com.java.rickjinny.model.mapper.SendRecordMapper;
import com.java.rickjinny.server.common.CommonService;
import com.java.rickjinny.server.enums.Constant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 短信验证码及时失效 已过有效期 的验证码
 **/
@Component
public class MessageCodeScheduler {

    private static final Logger log= LoggerFactory.getLogger(MessageCodeScheduler.class);

    @Autowired
    private SendRecordMapper recordMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 定时检测并失效已经过了有效期的验证码
     */
    @Async("threadPoolTaskExecutor")
    @Scheduled(cron = "0 0/30 * * * ?")
    public void schedulerCheckCode() {
        try {
            List<SendRecord> list = recordMapper.selectTimeoutCodes();
            if (list != null && !list.isEmpty()) {
                Set<Integer> set = list.stream().map(SendRecord::getId).collect(Collectors.toSet());
                String ids = Joiner.on(",").join(set);
                recordMapper.updateTimeoutCode(ids);
                // 发送mq消息，记录日志-以便管理员查询
                commonService.recordLog(ids, "短信验证码及时失效-定时任务-mq", "schedulerCheckCode");
            }
        } catch (Exception e) {
            log.error("定时检测并失效已经过了有效期的验证码-发生异常：", e);
        }
    }

    //redis的key过期失效-定时任务执行
    @Async("threadPoolTaskExecutor")
    //@Scheduled(cron = "0/30 * * * * ?")
    @Scheduled(cron = "0 0/30 * * * ?")
    public void schedulerCheckCodeV2(){
        try {
            List<SendRecord> list=recordMapper.selectAllActiveCodes();
            Set<Integer> ids=Sets.newHashSet();

            if (list!=null && !list.isEmpty()){
                list.forEach(code -> {
                    if (StringUtils.isNotBlank(code.getPhone())){
                        if (!redisTemplate.hasKey(Constant.RedisMsgCodeKey+code.getPhone())){
                            int res=recordMapper.updateExpireCode(code.getId());

                            if (res>0){
                                ids.add(code.getId());
                            }
                        }
                    }

                });
            }
            //发送mq消息，记录日志-以便管理员查询
            if (ids!=null && !ids.isEmpty()){
                String idArr=Joiner.on(",").join(ids);
                recordMapper.updateTimeoutCode(idArr);
                commonService.recordLog(idArr,"redis的key过期失效-定时任务执行-mq","schedulerCheckCodeV2");
            }
        }catch (Exception e){
            log.error("redis的key过期失效-定时任务执行-发生异常：",e);
        }
    }
}





















