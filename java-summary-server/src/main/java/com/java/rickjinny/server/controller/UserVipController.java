package com.java.rickjinny.server.controller;

import cn.hutool.core.util.StrUtil;
import com.java.rickjinny.api.response.BaseResponse;
import com.java.rickjinny.api.response.StatusCode;
import com.java.rickjinny.model.entity.UserVip;
import com.java.rickjinny.server.service.uservip.UserVipService;
import com.java.rickjinny.server.utils.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user/vip")
public class UserVipController extends AbstractController {

    @Autowired
    private UserVipService vipService;

    //充值会员
    @RequestMapping(value = "put",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse put(@RequestBody @Validated UserVip vip, BindingResult result){
        String checkRes= ValidatorUtil.checkResult(result);
        if (StrUtil.isNotBlank(checkRes)){
            return new BaseResponse(StatusCode.INVALID_PARAMS.getCode(), checkRes);
        }
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
//            vipService.addVip(vip);
            vipService.addVipV2(vip);
        } catch (Exception e) {
            logger.error("--充值会员-发生异常：", e.fillInStackTrace());
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }
}






























