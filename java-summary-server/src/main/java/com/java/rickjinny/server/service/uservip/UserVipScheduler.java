package com.java.rickjinny.server.service.uservip;//package com.debug.middleware.fight.one.server.service.userVip;/**
// * Created by Administrator on 2020/3/15.
// */
//
//import com.debug.middleware.fight.one.model.entity.UserVip;
//import com.debug.middleware.fight.one.model.mapper.UserVipMapper;
//import com.debug.middleware.fight.one.server.enums.Constant;
//import com.debug.middleware.fight.one.server.service.mail.MailService;
//import org.apache.commons.lang3.StringUtils;
//import org.redisson.api.RBlockingQueue;
//import org.redisson.api.RedissonClient;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
///**
// * 用户会员到期近实时的检测 - 检测cache
// * @Author:debug (SteadyJack)
// * @Link: weixin-> debug0868 qq-> 1948831260
// * @Date: 2020/3/15 18:34
// **/
//@Component
//public class UserVipScheduler {
//
//    private static final Logger log= LoggerFactory.getLogger(UserVipScheduler.class);
//
//    @Autowired
//    private UserVipMapper vipMapper;
//
//    @Autowired
//    private RedissonClient redisson;
//
//    @Autowired
//    private MailService mailService;
//
//    @Autowired
//    private Environment env;
//
//
//    //TODO:延迟队列监听  用户会员到期  的记录
//    @Async("threadPoolTaskExecutor")
//    @Scheduled(cron = "0/5 * * * * ?")
//    public void manageNotice(){
//        try {
//            //TODO:堵塞队列
//            RBlockingQueue<String> blockingQueue=redisson.getBlockingQueue(Constant.RedissonUserVipQueue);
//            if (blockingQueue!=null && !blockingQueue.isEmpty()) {
//                String element = blockingQueue.poll();
//
//                if (StringUtils.isNotBlank(element)){
//                    log.info("--延迟队列监听  用户会员到期  的记录 监听到数据：{}",element);
//
//                    String[] arr=StringUtils.split(element, Constant.SplitCharUserVip);
//                    Integer type=Integer.valueOf(arr[0]);
//                    Integer recordId=Integer.valueOf(arr[1]);
//
//
//                    UserVip vip=vipMapper.selectByPrimaryKey(recordId);
//                    if (vip!=null && StringUtils.isNotBlank(vip.getPhone()) && StringUtils.isNotBlank(vip.getEmail())){
//                        //区分是否是第一次的提醒
//                        if (Constant.VipExpireFlg.VipExpireNoticeFirst.getType().equals(type)){
//                            String content=String.format(env.getProperty("user.vip.expire.first.content"),vip.getPhone());
//                            mailService.sendSimpleEmail(env.getProperty("user.vip.expire.first.subject"),content,vip.getEmail());
//                        }else{
//                            int res=vipMapper.updateExpireVip(recordId);
//                            if (res>0){
//                                String content=String.format(env.getProperty("user.vip.expire.end.content"),vip.getPhone());
//                                mailService.sendSimpleEmail(env.getProperty("user.vip.expire.end.subject"),content,vip.getEmail());
//                            }
//                        }
//                    }
//                }
//            }
//        }catch (Exception e){
//            log.error("延迟队列监听  用户会员到期  的记录-发生异常：",e.fillInStackTrace());
//        }
//    }
//
//
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
