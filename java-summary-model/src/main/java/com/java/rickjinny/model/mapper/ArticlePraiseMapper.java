package com.java.rickjinny.model.mapper;

import com.java.rickjinny.model.entity.ArticlePraise;
import org.springframework.data.repository.query.Param;

public interface ArticlePraiseMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ArticlePraise record);

    int insertSelective(ArticlePraise record);

    ArticlePraise selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ArticlePraise record);

    int updateByPrimaryKey(ArticlePraise record);

    int cancelPraise(@Param("articleId") Integer articleId, @Param("userId") Integer userId);
}