package com.java.rickjinny.server.controller;

import com.java.rickjinny.api.response.BaseResponse;
import com.java.rickjinny.api.response.StatusCode;
import com.java.rickjinny.model.dto.PraiseDto;
import com.java.rickjinny.server.service.praise.PraiseService;
import com.java.rickjinny.server.utils.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞功能
 */
@RestController
@RequestMapping("/praise/article")
public class PraiseController {

    @Autowired
    private PraiseService praiseService;

    /**
     * 获取文章列表
     */
    @GetMapping("list")
    public BaseResponse articleList(){
        BaseResponse response=new BaseResponse(StatusCode.SUCCESS);
        try {
            response.setData(praiseService.getAll());
        }catch (Exception e){
            response=new BaseResponse(StatusCode.FAIL.getCode(),e.getMessage());
        }
        return response;
    }

    /**
     * 点赞文章
     */
    @RequestMapping(value = "on", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse praiseOn(@RequestBody @Validated PraiseDto dto, BindingResult result) {
        String checkRes = ValidatorUtil.checkResult(result);
        if (StringUtils.isNotBlank(checkRes)) {
            return new BaseResponse(StatusCode.INVALID_PARAMS.getCode(), checkRes);
        }
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            response.setData(praiseService.praiseOn(dto));
        } catch (Exception e) {
            e.printStackTrace();
            response = new BaseResponse(StatusCode.INVALID_PARAMS.getCode(), e.getMessage());
        }
        return response;
    }

    /**
     * 取消点赞文章
     */
    @RequestMapping(value = "cancel",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse praiseCancel(@RequestBody @Validated PraiseDto dto, BindingResult result){
        String checkRes = ValidatorUtil.checkResult(result);
        if (StringUtils.isNotBlank(checkRes)){
            return new BaseResponse(StatusCode.INVALID_PARAMS.getCode(),checkRes);
        }
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);
        try {
            response.setData(praiseService.praiseCancel(dto));
        } catch (Exception e) {
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }

    //获取文章详情~排行榜
    @RequestMapping(value = "info",method = RequestMethod.GET)
    public BaseResponse articleInfo(@RequestParam Integer articleId, Integer currUserId){
        if (articleId==null || articleId<=0){
            return new BaseResponse(StatusCode.INVALID_PARAMS);
        }
        BaseResponse response=new BaseResponse(StatusCode.SUCCESS);
        try {
            response.setData(praiseService.getArticleInfo(articleId,currUserId));

        }catch (Exception e){
            response=new BaseResponse(StatusCode.FAIL.getCode(),e.getMessage());
        }
        return response;
    }

    //获取用户点赞过的历史文章-用户详情
    @RequestMapping(value = "user/articles",method = RequestMethod.GET)
    public BaseResponse userArticles(@RequestParam Integer currUserId){
        if (currUserId==null || currUserId<=0){
            return new BaseResponse(StatusCode.INVALID_PARAMS);
        }
        BaseResponse response=new BaseResponse(StatusCode.SUCCESS);
        try {
            response.setData(praiseService.getUserArticles(currUserId));

        }catch (Exception e){
            response=new BaseResponse(StatusCode.FAIL.getCode(),e.getMessage());
        }
        return response;
    }
}
