package com.java.rickjinny.server.controller;

import com.java.rickjinny.api.response.BaseResponse;
import com.java.rickjinny.api.response.StatusCode;
import com.java.rickjinny.server.service.message.MessageCodeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信验证码
 */
@RestController
@RequestMapping("/message/code")
public class MessageCodeController extends AbstractController {

    @Autowired
    private MessageCodeService messageCodeService;

    //发送短信验证码到提供的手机号
    @RequestMapping("send")
    public BaseResponse sendCode(@RequestParam String phone){
        if (StringUtils.isBlank(phone)){
            return new BaseResponse(StatusCode.INVALID_PARAMS);
        }
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            //response.setData(messageCodeService.getRandomCodeV1(phone));
            //response.setData(messageCodeService.getRandomCodeV2(phone));
            response.setData(messageCodeService.getRandomCodeV3(phone));
        }catch (Exception e){
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }

    //验证短信验证码
    @RequestMapping("validate")
    public BaseResponse validateCode(@RequestParam String phone, @RequestParam String code){
        if (StringUtils.isBlank(phone) || StringUtils.isBlank(code)){
            return new BaseResponse(StatusCode.INVALID_PARAMS);
        }
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            //response.setData(msgCodeService.validateCodeV1(phone,code));

            //response.setData(msgCodeService.validateCodeV2(phone,code));

            response.setData(messageCodeService.validateCodeV3(phone,code));

        }catch (Exception e){
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }
}
