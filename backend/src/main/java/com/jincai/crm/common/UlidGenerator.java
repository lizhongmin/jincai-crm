package com.jincai.crm.common;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * ULID 生成器工具类。
 * <p>
 * ULID (Universally Unique Lexicographically Sortable Identifier) 具有以下特点：
 * <ul>
 *   <li>26 个字符的字符串表示</li>
 *   <li>按创建时间自然有序（前 10 个字符为时间戳）</li>
 *   <li>不可预测（后 16 个字符为随机数）</li>
 *   <li>比 UUID 更短更紧凑</li>
 * </ul>
 */
public final class UlidGenerator {

    private UlidGenerator() {
    }

    /**
     * 生成一个新的 ULID 字符串。
     *
     * @return 26 个字符的 ULID 字符串，如 "01ARZ3NDEKTSV4RRFFQ69G5FAV"
     */
    public static String generate() {
        return UlidCreator.getMonotonicUlid().toString();
    }
}
