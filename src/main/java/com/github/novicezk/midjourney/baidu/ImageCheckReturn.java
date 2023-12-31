package com.github.novicezk.midjourney.baidu;

import java.util.List;

/**
 * 图像审核结果返回
 */
public class ImageCheckReturn {

    private long log_id;//请求唯一id，用于问题定位
    private String conclusion;//审核结果，可取值：合规、不合规、疑似、审核失败
    private Integer conclusionType;//审核结果类型，可取值1.合规，2.不合规，3.疑似，4.审核失败
//    private List<ImageData> data;//不合规/疑似/命中白名单项详细信息

    private String error_msg;

    private String error_code;

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

//    public List<ImageData> getData() {
//        return data;
//    }

//    public void setData(List<ImageData> data) {
//        this.data = data;
//    }


    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    @Override
    public String toString() {
        return "{" +
                "log_id=" + log_id +
                ", conclusion='" + conclusion + '\'' +
                ", conclusionType=" + conclusionType +
//                ", data=" + data +
                '}';
    }
}