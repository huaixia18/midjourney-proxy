package com.github.novicezk.midjourney.baidu;

import java.util.List;

/**
 * 文本审核结果返回
 */
public class TextCheckReturn {

    private long log_id;//请求唯一id，用于问题定位
    private String conclusion;//审核结果，可取值：合规、不合规、疑似、审核失败
    private Integer conclusionType;//审核结果类型，可取值1.合规，2.不合规，3.疑似，4.审核失败
    private List<TextData> data;//不合规/疑似/命中白名单项详细信息

    public long getLog_id() {
        return log_id;
    }

    public void setLog_id(long log_id) {
        this.log_id = log_id;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public Integer getConclusionType() {
        return conclusionType;
    }

    public void setConclusionType(Integer conclusionType) {
        this.conclusionType = conclusionType;
    }

    public List<TextData> getData() {
        return data;
    }

    public void setData(List<TextData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" +
                "log_id=" + log_id +
                ", conclusion='" + conclusion + '\'' +
                ", conclusionType=" + conclusionType +
                ", data=" + data +
                '}';
    }
}