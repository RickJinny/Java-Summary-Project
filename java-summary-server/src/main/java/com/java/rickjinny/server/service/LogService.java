package com.java.rickjinny.server.service;

import com.java.rickjinny.model.entity.SysLog;
import com.java.rickjinny.model.mapper.SysLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    @Autowired
    private SysLogMapper sysLogMapper;

    // 记录日志
    public void recordLog(SysLog sysLog) {
        sysLogMapper.insertSelective(sysLog);
    }
}
