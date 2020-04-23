package com.java.rickjinny.model.mapper;


import com.java.rickjinny.model.entity.SendRecord;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SendRecordMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(SendRecord record);

    int insertSelective(SendRecord record);

    SendRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SendRecord record);

    int updateByPrimaryKey(SendRecord record);

    SendRecord selectByPhoneCode(@Param("phone") String phone, @Param("code") String code);

    List<SendRecord> selectTimeoutCodes();

    int updateTimeoutCode(@Param("ids") String ids);

    List<SendRecord> selectAllActiveCodes();

    int updateExpireCode(@Param("id") Integer id);

    int updateExpirePhoneCode(@Param("phone") String phone, @Param("code") String code);
}