package com.github.novicezk.midjourney.baidu;

public enum ReturnStatusEnum {

    SUCCESS(0),//成功
    FAILURE(1);//失败

    private final int value;

    ReturnStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
