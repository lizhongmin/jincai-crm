package com.jincai.crm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT Token 服务。
 *
 * <p>负责 Token 的生成、解析与验证，使用 HMAC-SHA256 (HS256) 算法对称签名。
 *
 * <p><b>密钥安全要求</b>：
 * <ul>
 *   <li>密钥长度不得少于 32 字节（256 位），否则启动时会抛出 {@link IllegalStateException}。</li>
 *   <li>支持两种密钥格式：
 *     <ul>
 *       <li>普通字符串：直接使用 UTF-8 字节，确保字符串本身至少 32 字节长。</li>
 *       <li>Base64 编码：以 {@code base64:} 为前缀，解码后字节数不得少于 32。</li>
 *     </ul>
 *   </li>
 *   <li>生产环境必须通过环境变量 {@code JWT_SECRET} 注入，不得硬编码或使用弱密钥。</li>
 *   <li>生成强密钥示例：{@code openssl rand -hex 32}</li>
 * </ul>
 *
 * <p><b>Token 结构</b>：
 * <ul>
 *   <li>{@code sub}：登录用户名</li>
 *   <li>{@code iat}：签发时间戳</li>
 *   <li>{@code exp}：过期时间戳（由 {@code app.jwt.expiration-seconds} 配置，默认 86400 秒）</li>
 * </ul>
 */
@Slf4j
@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    /**
     * 应用启动后立即校验 JWT 密钥的合法性。
     * 若密钥为空或长度不足，直接抛出异常阻止应用启动，防止在运行时才暴露问题。
     */
    @PostConstruct
    public void validateSecretOnStartup() {
        String secret = properties.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET 未配置！请通过环境变量设置，示例：export JWT_SECRET=$(openssl rand -hex 32)");
        }
        // 触发一次 signingKey() 以验证长度合规，启动失败优于运行时失败
        signingKey();
        log.info("JWT 密钥校验通过，Token 有效期：{} 秒", properties.expirationSeconds());
    }

    /**
     * 为指定用户名生成 JWT Token。
     *
     * @param username 登录用户名，将作为 {@code sub} 声明写入 Token
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.expirationSeconds());
        return Jwts.builder()
            .subject(username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey())
            .compact();
    }

    /**
     * 从 Token 中解析登录用户名。
     *
     * @param token JWT 字符串
     * @return 用户名（{@code sub} 声明的值）
     * @throws JwtException 若 Token 无效或已过期
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 校验 Token 是否合法（签名正确且未过期）。
     *
     * @param token JWT 字符串
     * @return {@code true} 表示 Token 有效；{@code false} 表示 Token 无效或已过期
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            log.debug("JWT 校验失败：{}", ex.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------ private

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * 根据配置构建 HMAC-SHA256 签名密钥。
     *
     * <p>密钥格式支持：
     * <ul>
     *   <li>{@code base64:<base64编码字符串>}：先 Base64 解码，再构建密钥。</li>
     *   <li>普通字符串：直接以 UTF-8 字节构建密钥。</li>
     * </ul>
     *
     * @throws IllegalStateException 若密钥长度不足 32 字节（HMAC-SHA256 最低要求）
     */
    private SecretKey signingKey() {
        String secret = properties.secret();
        byte[] secretBytes;

        if (secret != null && secret.startsWith("base64:")) {
            secretBytes = Decoders.BASE64.decode(secret.substring("base64:".length()));
        } else {
            secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                "JWT 密钥长度不足：当前 " + secretBytes.length + " 字节，HMAC-SHA256 要求至少 32 字节（256 位）。" +
                "请通过 JWT_SECRET 环境变量配置更强的密钥。");
        }

        return Keys.hmacShaKeyFor(secretBytes);
    }
}
