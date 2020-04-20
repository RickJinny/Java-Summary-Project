package com.java.rickjinny.model.mapper;

import com.java.rickjinny.model.entity.UserVip;
import org.springframework.data.repository.query.Param;

public interface UserVipMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(UserVip record);

    int insertSelective(UserVip record);

    UserVip selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserVip record);

    int updateByPrimaryKey(UserVip record);

    int updateExpireVip(@Param("id") Integer id);
}