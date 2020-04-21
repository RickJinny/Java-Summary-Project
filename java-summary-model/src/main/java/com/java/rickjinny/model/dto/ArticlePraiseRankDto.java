package com.java.rickjinny.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文章点赞排行榜信息
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ArticlePraiseRankDto implements Serializable{

    //文章id
    private String articleId;

    //文章标题
    private String title;

    //点赞总数
    private String total;

    private Double score;
}