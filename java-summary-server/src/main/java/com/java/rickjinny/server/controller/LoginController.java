package com.java.rickjinny.server.controller;

import com.java.rickjinny.api.response.BaseResponse;
import com.java.rickjinny.api.response.StatusCode;
import com.java.rickjinny.model.entity.User;
import com.java.rickjinny.model.mapper.SysLogMapper;
import com.java.rickjinny.model.mapper.UserMapper;
import com.java.rickjinny.server.service.log.LogAopAnnotation;
import com.java.rickjinny.server.service.log.LogApplicationEvent;
import com.java.rickjinny.server.utils.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志记录
 */
@RestController
@RequestMapping("/login")
public class LoginController extends AbstractController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysLogMapper sysLogMapper;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    // 用户操作 - 新增用户 - 才会产生日志
    @RequestMapping("/user/add")
    public BaseResponse addUser(@RequestBody @Validated User user, BindingResult result) {
        String checkResult = ValidatorUtil.checkResult(result);
        if (StringUtils.isNotBlank(checkResult)) {
            return new BaseResponse(StatusCode.INVALID_PARAMS.getCode(), checkResult);
        }
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            int res = userMapper.insertSelective(user);
            if (res > 0) {
                // 插入成功之后 我们需要记录当前用户的操作日志
                LogApplicationEvent logApplicationEvent = new LogApplicationEvent(this, "debug",
                        "新增用户", "addUser");
                applicationEventPublisher.publishEvent(logApplicationEvent);
            }
        } catch (Exception e) {
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }

    // 用户操作 - 新增用户 - 才会产生操作日志 (spring aop)
    @RequestMapping("/user/add/aop")
    @LogAopAnnotation("新增用户-spring aop")
    public BaseResponse addUserV2(@RequestBody @Validated User user, BindingResult result) {
        String checkResult = ValidatorUtil.checkResult(result);
        if (StringUtils.isNotBlank(checkResult)) {
            return new BaseResponse(StatusCode.INVALID_PARAMS.getCode(), checkResult);
        }
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            userMapper.insertSelective(user);
        } catch (Exception e) {
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }
}
