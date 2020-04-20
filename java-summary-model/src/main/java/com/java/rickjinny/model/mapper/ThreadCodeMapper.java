package com.java.rickjinny.model.mapper;


import com.java.rickjinny.model.entity.ThreadCode;
import org.springframework.data.repository.query.Param;

public interface ThreadCodeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ThreadCode record);

    int insertSelective(ThreadCode record);

    ThreadCode selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ThreadCode record);

    int updateByPrimaryKey(ThreadCode record);

    int insertCode(@Param("code") String code);
}