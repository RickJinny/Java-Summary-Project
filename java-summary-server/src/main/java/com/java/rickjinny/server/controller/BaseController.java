package com.java.rickjinny.server.controller;

import com.java.rickjinny.api.response.BaseResponse;
import com.java.rickjinny.api.response.StatusCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/base")
public class BaseController {

    public static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @RequestMapping("/info")
    public BaseResponse info(String name) {
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            if (StringUtils.isBlank(name)) {
                name = "Result";
            }
            response.setData(name);
        } catch (Exception e) {
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }
}
