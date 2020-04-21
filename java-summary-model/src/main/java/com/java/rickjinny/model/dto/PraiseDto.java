package com.java.rickjinny.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PraiseDto {

    @NotNull(message = "当前用户id不能为空!")
    private Integer userId;

    @NotNull(message = "当前文章id不能为空!")
    private Integer articleId;

    // 文章标题 - 开发技巧 - 服务于排行榜(如微博的热搜，只显示其标题，而不需要再根据id查询db获取标题...)
    @NotNull(message = "当前文章标题不能为空!")
    private String title;

    public PraiseDto(Integer userId, Integer articleId, String title) {
        this.userId = userId;
        this.articleId = articleId;
        this.title = title;
    }
}
