package com.java.rickjinny.server.controller;

import com.java.rickjinny.api.response.BaseResponse;
import com.java.rickjinny.api.response.StatusCode;
import com.java.rickjinny.model.entity.User;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志记录
 */
@RestController
@RequestMapping("/login")
public class LoginController extends AbstractController {

    // 用户操作 - 新增用户 - 才会产生日志
    @RequestMapping("/user/add")
    public BaseResponse addUser(User user, BindResult result) {
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            // TODO: 写核心业务逻辑

        } catch (Exception e) {
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }
}
