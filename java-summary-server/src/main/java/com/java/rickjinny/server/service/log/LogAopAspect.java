package com.java.rickjinny.server.service.log;

import com.google.gson.Gson;
import com.java.rickjinny.model.entity.SysLog;
import com.java.rickjinny.server.enums.Constant;
import com.java.rickjinny.server.service.LogService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 模块化的东西 - 类 - 切面 - 将关注点从核心业务逻辑中分离出来
 */
@Aspect
@Component
public class LogAopAspect {

    @Autowired
    private LogService logService;

    /**
     * 切点: 使用特定注解的地方将触发通知(做日志的记录) - 切点
     */
    @Pointcut("@annotation(com.java.rickjinny.server.service.log.LogAopAnnotation)")
    public void logPointCut() {

    }

    /**
     * 通知: 环绕通知(前置通知 + 后置通知的结合), 其实就是我们指定的注解，所在的方法 执行前 + 执行后 , 进行监控
     */
    @Around("logPointCut()")
    public Object executeAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        Long time = System.currentTimeMillis() - startTime;
        saveLog(joinPoint, time, result);
        return result;
    }

    // 记录日志: AOP - 动态代理 - 底层还是由反射实现的
    private void saveLog(ProceedingJoinPoint joinPoint, Long time, Object res) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SysLog entity = new SysLog();
        // 获取注解上用户操作描述
        LogAopAnnotation annotation = method.getAnnotation(LogAopAnnotation.class);
        if (annotation != null) {
            entity.setOperation(annotation.value());
        }
        // 获取操作的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        entity.setMethod(new StringBuilder(className).append(".")
                .append(methodName).append("()").toString());
        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        String params = new Gson().toJson(args[0]);
        entity.setParams(params);
        // 获取剩下的参数
        entity.setTime(time);
        entity.setUsername(Constant.logOperateUser);
        entity.setCreateDate(DateTime.now().toDate());
        // 方法的执行结果
        if (res != null && StringUtils.isNotBlank(res.toString())) {
            entity.setMemo(new Gson().toJson(res));
        }
        logService.recordLog(entity);
    }
}
