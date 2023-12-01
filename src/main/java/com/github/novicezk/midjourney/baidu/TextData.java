package com.github.novicezk.midjourney.baidu;

import java.util.List;

/**
 * 不合规/疑似/命中白名单项详细信息
 */
public class TextData {

    private String msg;//不合规项描述信息
    private List<Hits> hits;//命中关键词信息

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Hits> getHits() {
        return hits;
    }

    public void setHits(List<Hits> hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        return "{" +
                "msg='" + msg + '\'' +
                ", hits=" + hits +
                '}';
    }
}
