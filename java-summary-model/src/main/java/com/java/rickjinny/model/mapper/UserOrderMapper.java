package com.java.rickjinny.model.mapper;


import com.java.rickjinny.model.entity.UserOrder;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserOrderMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(UserOrder record);

    int insertSelective(UserOrder record);

    UserOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserOrder record);

    int updateByPrimaryKey(UserOrder record);

    List<UserOrder> selectUnPayOrders();

    int unActiveOrder(@Param("id") Integer id);
}