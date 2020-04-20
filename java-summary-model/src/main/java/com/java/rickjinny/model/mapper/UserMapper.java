package com.java.rickjinny.model.mapper;


import com.java.rickjinny.model.entity.User;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    List<User> selectList();

    User selectByEmail(@Param("email") String email);

    String selectNamesById(@Param("ids") String ids);
}