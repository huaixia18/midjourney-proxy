package com.github.novicezk.midjourney.dto.wenxin;

import java.util.List;

/**
 * @author biliyu
 * @date 2023/10/19 16:59
 */
public class QianFanChatDto {

    /**
     * 本轮对话的id
     */
    private String id;

    /**
     * 回包类型
     * chat.completion：多轮对话返回
     */
    private String object;

    /**
     * 时间戳
     */
    private Integer created;

    /**
     * 表示当前子句的序号。只有在流式接口模式下会返回该字段
     */
    private Integer sentence_id;

    /**
     * 表示当前子句是否是最后一句。只有在流式接口模式下会返回该字段
     */
    private Boolean is_end;

    /**
     * 当前生成的结果是否被截断
     */
    private Boolean is_truncated;

    /**
     * 对话返回结果
     */
    private String result;

    /**
     * 表示用户输入是否存在安全，是否关闭当前会话，清理历史会话信息
     * true：是，表示用户输入存在安全风险，建议关闭当前会话，清理历史会话信息
     * false：否，表示用户输入无安全风险
     */
    private Boolean need_clear_history;


    /**
     * 当need_clear_history为true时，此字段会告知第几轮对话有敏感信息，如果是当前问题，ban_round=-1
     */
    private Integer ban_round;

    /**
     * token统计信息，token数 = 汉字数+单词数*1.3 （仅为估算逻辑）
     */
    private usage usage;

    /**
     * 由模型生成的函数调用，包含函数名称，和调用参数
     */
    private function_call function_call;

    static class usage {

        /**
         * 问题tokens数
         */
        private Integer prompt_tokens;

        /**
         * 回答tokens数
         */
        private Integer completion_tokens;

        /**
         * tokens总数
         */
        private Integer total_tokens;

        /**
         * plugin消耗的tokens
         */
        private List<plugin_usage> plugins;

        public Integer getPrompt_tokens() {
            return prompt_tokens;
        }

        public void setPrompt_tokens(Integer prompt_tokens) {
            this.prompt_tokens = prompt_tokens;
        }

        public Integer getCompletion_tokens() {
            return completion_tokens;
        }

        public void setCompletion_tokens(Integer completion_tokens) {
            this.completion_tokens = completion_tokens;
        }

        public Integer getTotal_tokens() {
            return total_tokens;
        }

        public void setTotal_tokens(Integer total_tokens) {
            this.total_tokens = total_tokens;
        }

        public List<plugin_usage> getPlugins() {
            return plugins;
        }

        public void setPlugins(List<plugin_usage> plugins) {
            this.plugins = plugins;
        }
    }

    static class plugin_usage {

        /**
         * plugin名称，chatFile：chatfile插件消耗的tokens
         */
        private String name;

        /**
         * 解析文档tokens
         */
        private Integer parse_tokens;

        /**
         * 摘要文档tokens
         */
        private Integer abstract_tokens;

        /**
         * 检索文档tokens
         */
        private Integer search_tokens;

        /**
         * 总 token
         */
        private Integer total_tokens;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getParse_tokens() {
            return parse_tokens;
        }

        public void setParse_tokens(Integer parse_tokens) {
            this.parse_tokens = parse_tokens;
        }

        public Integer getAbstract_tokens() {
            return abstract_tokens;
        }

        public void setAbstract_tokens(Integer abstract_tokens) {
            this.abstract_tokens = abstract_tokens;
        }

        public Integer getSearch_tokens() {
            return search_tokens;
        }

        public void setSearch_tokens(Integer search_tokens) {
            this.search_tokens = search_tokens;
        }

        public Integer getTotal_tokens() {
            return total_tokens;
        }

        public void setTotal_tokens(Integer total_tokens) {
            this.total_tokens = total_tokens;
        }
    }

    static class function_call {

        /**
         * 触发的function名
         */
        private String name;

        /**
         * 模型思考过程
         */
        private String thoughts;

        /**
         * 请求参数
         */
        private String arguments;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getThoughts() {
            return thoughts;
        }

        public void setThoughts(String thoughts) {
            this.thoughts = thoughts;
        }

        public String getArguments() {
            return arguments;
        }

        public void setArguments(String arguments) {
            this.arguments = arguments;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public Integer getSentence_id() {
        return sentence_id;
    }

    public void setSentence_id(Integer sentence_id) {
        this.sentence_id = sentence_id;
    }

    public Boolean getIs_end() {
        return is_end;
    }

    public void setIs_end(Boolean is_end) {
        this.is_end = is_end;
    }

    public Boolean getIs_truncated() {
        return is_truncated;
    }

    public void setIs_truncated(Boolean is_truncated) {
        this.is_truncated = is_truncated;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Boolean getNeed_clear_history() {
        return need_clear_history;
    }

    public void setNeed_clear_history(Boolean need_clear_history) {
        this.need_clear_history = need_clear_history;
    }

    public Integer getBan_round() {
        return ban_round;
    }

    public void setBan_round(Integer ban_round) {
        this.ban_round = ban_round;
    }

    public QianFanChatDto.usage getUsage() {
        return usage;
    }

    public void setUsage(QianFanChatDto.usage usage) {
        this.usage = usage;
    }

    public QianFanChatDto.function_call getFunction_call() {
        return function_call;
    }

    public void setFunction_call(QianFanChatDto.function_call function_call) {
        this.function_call = function_call;
    }
}
