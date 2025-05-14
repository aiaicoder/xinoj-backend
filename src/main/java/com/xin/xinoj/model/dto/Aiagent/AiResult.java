package com.xin.xinoj.model.dto.Aiagent;

import java.util.List;

public class AiResult {
    private Integer code;
    private String message;
    private String sid;
    private String id;
    private Long created;
    private List<AiResultChoices> choices;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public List<AiResultChoices> getChoices() {
        return choices;
    }

    public void setChoices(List<AiResultChoices> choices) {
        this.choices = choices;
    }
}
