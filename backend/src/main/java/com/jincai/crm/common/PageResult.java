package com.jincai.crm.common;

import java.util.List;

public record PageResult<T>(
    List<T> items,
    long total,
    int page,
    int size
) {
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> items, long total, int page, int size) {
        return new PageResult<>(items, total, page, size);
    }
}

