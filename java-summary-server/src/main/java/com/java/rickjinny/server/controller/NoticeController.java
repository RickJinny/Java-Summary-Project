package com.java.rickjinny.server.controller;

import com.java.rickjinny.api.response.BaseResponse;
import com.java.rickjinny.api.response.StatusCode;
import com.java.rickjinny.model.entity.Notice;
import com.java.rickjinny.model.mapper.NoticeMapper;
import com.java.rickjinny.server.enums.Constant;
import com.java.rickjinny.server.utils.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告通知消息通知
 */
@RestController
@RequestMapping("/notice")
public class NoticeController extends AbstractController {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    // 系统管理员添加公告 - redis订阅发布机制 - 发送接收消息
    @RequestMapping("add")
    public BaseResponse addNotice(@RequestBody @Validated Notice notice, BindingResult result){
        String checkRes = ValidatorUtil.checkResult(result);
        if (StringUtils.isNotBlank(checkRes)){
            return new BaseResponse(StatusCode.INVALID_PARAMS.getCode(), checkRes);
        }
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            int res = noticeMapper.insertSelective(notice);
            if (res > 0) {
                //生产者发布消息-pub
                redisTemplate.convertAndSend(Constant.RedisTopicNameEmail, notice);
            }
        }catch (Exception e){
            logger.error("系统管理员添加公告-redis订阅发布机制-发送接收消息：", e);
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }
}
