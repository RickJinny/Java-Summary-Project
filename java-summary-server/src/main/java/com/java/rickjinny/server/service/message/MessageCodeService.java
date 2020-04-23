package com.java.rickjinny.server.service.message;

import com.java.rickjinny.model.entity.SendRecord;
import com.java.rickjinny.model.mapper.SendRecordMapper;
import com.java.rickjinny.server.enums.Constant;
import com.java.rickjinny.server.utils.RandomUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码 - service
 **/
@Service
public class MessageCodeService {

    @Autowired
    private SendRecordMapper recordMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    /**
     * 获取短信验证码 - 传统的方式: sql 操作 + 定时任务调度检测
     * @param phone
     * @return
     * @throws Exception
     */
    public String getRandomCodeV1(final String phone) throws Exception{
        // TODO：在网络不稳定的情况下，可能手机没能接收到短信验证码，此时需要重新申请，即
        // TODO: 同个手机号多次申请验证码，如果该手机号存在着  30min内有效的验证码，则直接取出发给他即可，而无需重新生成，
        // TODO: 以免造成空间的浪费
        SendRecord record = recordMapper.selectByPhoneCode(phone, null);
        if (record != null && StringUtils.isNotBlank(record.getCode())) {
            return record.getCode();
        }
        // 否则的话，重新生成新的 4 位短信验证码
        String msgCode = RandomUtil.randomMsgCode(4);

        SendRecord entity = new SendRecord(phone, msgCode);
        entity.setSendTime(DateTime.now().toDate());
        recordMapper.insertSelective(entity);

        // 调用短信供应商提供的发送短信的api - 阿里云sms、网建短信通...

        return msgCode;
    }

    /**
     * 校验短信验证码-有效且在30min内
     * @param phone
     * @param code
     * @return
     * @throws Exception
     */
    public Boolean validateCodeV1(final String phone,final String code) throws Exception{
        SendRecord record = recordMapper.selectByPhoneCode(phone, code);
        return record != null;
    }

    /**
     * 获取短信验证码 - 传统的方式: redis的 key 过期失效 + 定时任务调度检测
     */
    public String getRandomCodeV2(final String phone) throws Exception{
        ValueOperations<String, String> phoneOpera = redisTemplate.opsForValue();

        //TODO：在网络不稳定的情况下，可能手机没能接收到短信验证码，此时需要重新申请，即
        //TODO: 同个手机号多次申请验证码，如果该手机号存在着  30min内有效的验证码，则直接取出发给他即可，而无需重新生成，
        //TODO: 以免造成空间的浪费
        final String key = Constant.RedisMsgCodeKey + phone;
        if (redisTemplate.hasKey(key)) {
            return phoneOpera.get(key);
        }

        //否则的话，重新生成新的 4位短信验证码
        String msgCode = RandomUtil.randomMsgCode(4);
        SendRecord entity = new SendRecord(phone, msgCode);
        entity.setSendTime(DateTime.now().toDate());
        int res = recordMapper.insertSelective(entity);
        if (res > 0) {
            //phoneOpera.set(key,msgCode,30L,TimeUnit.MINUTES);
            phoneOpera.set(key, msgCode, 1L, TimeUnit.MINUTES);
        }

        //调用短信供应商提供的发送短信的api - 阿里云sms、网建短信通...


        return msgCode;
    }

    /**
     * 校验短信验证码-有效且在30min内
     *
     * @param phone
     * @param code
     * @return
     * @throws Exception
     */
    public Boolean validateCodeV2(final String phone, final String code) throws Exception {
        ValueOperations<String, String> phoneOpera = redisTemplate.opsForValue();
        final String key = Constant.RedisMsgCodeKey + phone;
        String cacheCode = phoneOpera.get(key);
        return StringUtils.isNotBlank(cacheCode) && cacheCode.equals(code);
    }

    /**
     * 获取短信验证码-传统的方式: redis 的 key 过期失效 + 定时任务调度检测
     * @param phone
     * @return
     * @throws Exception
     */
    public String getRandomCodeV3(final String phone) throws Exception{
        RMapCache<String, String> rMapCache = redisson.getMapCache(Constant.RedissonMsgCodeKey);

        //TODO：在网络不稳定的情况下，可能手机没能接收到短信验证码，此时需要重新申请，即
        //TODO: 同个手机号多次申请验证码，如果该手机号存在着  30min内有效的验证码，则直接取出发给他即可，而无需重新生成，
        //TODO: 以免造成空间的浪费
        String code = rMapCache.get(phone);
        if (StringUtils.isNotBlank(code)) {
            return code;
        }

        //否则的话，重新生成新的 4位短信验证码
        String msgCode = RandomUtil.randomMsgCode(4);

        SendRecord entity = new SendRecord(phone, msgCode);
        entity.setSendTime(DateTime.now().toDate());
        int res = recordMapper.insertSelective(entity);
        if (res > 0) {
            //rMapCache.put(phone,msgCode,30L,TimeUnit.MINUTES);
            rMapCache.put(phone, msgCode, 1L, TimeUnit.MINUTES);
        }

        //调用短信供应商提供的发送短信的api - 阿里云sms、网建短信通...

        return msgCode;
    }

    /**
     * 校验短信验证码 - 有效且在 30min 内
     * @param phone
     * @param code
     * @return
     * @throws Exception
     */
    public Boolean validateCodeV3(final String phone, final String code) throws Exception {
        RMapCache<String, String> rMapCache = redisson.getMapCache(Constant.RedissonMsgCodeKey);
        String cacheCode = rMapCache.get(phone);
        return StringUtils.isNotBlank(cacheCode) && cacheCode.equals(code);
    }
}
