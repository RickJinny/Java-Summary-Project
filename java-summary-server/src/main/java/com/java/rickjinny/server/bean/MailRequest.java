package com.java.rickjinny.server.bean;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@Data
public class MailRequest implements Serializable{

    @NotBlank(message = "用户邮箱不能为空！")
    private String userMails;

    @NotBlank(message = "邮件主题不能为空！")
    private String subject;

    @NotBlank(message = "邮件内容不能为空！")
    private String content;

}