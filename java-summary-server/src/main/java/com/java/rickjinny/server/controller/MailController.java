package com.java.rickjinny.server.controller;

import com.java.rickjinny.api.response.BaseResponse;
import com.java.rickjinny.api.response.StatusCode;
import com.java.rickjinny.server.bean.MailRequest;
import com.java.rickjinny.server.utils.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件发送
 **/
@RestController
@RequestMapping("/mail")
public class MailController extends AbstractController {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    /**
     * 邮件发送 - 直接指定要发送邮件的 邮箱
     */
    @RequestMapping("/send/mq")
    public BaseResponse stringData(@RequestBody @Validated MailRequest request, BindingResult result){
        String checkRes = ValidatorUtil.checkResult(result);
        if (StringUtils.isNotBlank(checkRes)){
            return new BaseResponse(StatusCode.INVALID_PARAMS.getCode(),checkRes);
        }
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            // 直接将邮件信息充当消息，塞入MQ Server里去  -> 生产者
            rabbitTemplate.setExchange(env.getProperty("mq.email.exchange"));
            rabbitTemplate.setRoutingKey(env.getProperty("mq.email.routing.key"));
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.convertAndSend(request, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties properties = message.getMessageProperties();
                    // 设置消息持久化与消息头的类型
                    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    properties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, MailRequest.class);
                    return message;
                }
            });
        }catch (Exception e){
            logger.error("异常信息：",e);
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }
}