package com.github.novicezk.midjourney.baidu;

import java.util.List;

/**
 * 命中关键词信息
 */
public class Hits {

    private String datasetName;//违规项目所属数据集名称
    private List<String> words;//违规文本关键字

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    @Override
    public String toString() {
        return "{" +
                "datasetName='" + datasetName + '\'' +
                ", words=" + words +
                '}';
    }
}