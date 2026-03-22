package com.jincai.crm.auth.service;

import com.jincai.crm.auth.dto.CaptchaResponse;
import com.jincai.crm.auth.dto.LoginStateResponse;
import com.jincai.crm.common.BusinessException;
import com.jincai.crm.system.entity.LoginSecurityPolicy;
import com.jincai.crm.system.service.LoginSecurityPolicyService;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginSecurityService {

    private static final Duration FAIL_COUNT_TTL = Duration.ofHours(24);
    private static final String KEY_FAIL_PREFIX = "auth:login:fail:";
    private static final String KEY_LOCK_PREFIX = "auth:login:lock:";
    private static final String KEY_CAPTCHA_PREFIX = "auth:login:captcha:";

    private final StringRedisTemplate redisTemplate;
    private final LoginSecurityPolicyService policyService;

    public LoginSecurityService(StringRedisTemplate redisTemplate, LoginSecurityPolicyService policyService) {
        this.redisTemplate = redisTemplate;
        this.policyService = policyService;
    }

    public LoginStateResponse getLoginState(String username) {
        String normalized = normalizeUsername(username);
        if (normalized == null) {
            return new LoginStateResponse(false, false, 0);
        }

        LoginSecurityPolicy policy = policyService.getPolicyEntity();
        long lockSeconds = getLockSeconds(normalized);
        if (lockSeconds > 0) {
            return new LoginStateResponse(false, true, lockSeconds);
        }

        long failCount = getFailCount(normalized);
        boolean captchaRequired = failCount >= policy.getCaptchaAfterFailures();
        return new LoginStateResponse(captchaRequired, false, 0);
    }

    public void ensureLoginAllowedAndValidateCaptcha(String username, String captchaId, String captchaCode) {
        LoginStateResponse state = getLoginState(username);
        if (state.locked()) {
            throw new BusinessException("error.auth.accountLocked", toLockMinutes(state.lockSeconds()));
        }
        if (state.captchaRequired()) {
            validateCaptcha(username, captchaId, captchaCode);
        }
    }

    public LoginStateResponse onLoginFailure(String username) {
        String normalized = normalizeUsername(username);
        if (normalized == null) {
            return new LoginStateResponse(false, false, 0);
        }

        LoginSecurityPolicy policy = policyService.getPolicyEntity();
        String failKey = failKey(normalized);
        Long failCount = redisTemplate.opsForValue().increment(failKey);
        if (failCount != null && failCount == 1L) {
            redisTemplate.expire(failKey, FAIL_COUNT_TTL);
        }

        long current = failCount == null ? 0 : failCount;
        if (current >= policy.getLockAfterFailures()) {
            redisTemplate.delete(failKey);
            redisTemplate.opsForValue().set(lockKey(normalized), "1", Duration.ofMinutes(policy.getLockMinutes()));
            return new LoginStateResponse(false, true, policy.getLockMinutes() * 60L);
        }
        return new LoginStateResponse(current >= policy.getCaptchaAfterFailures(), false, 0);
    }

    public void onLoginSuccess(String username) {
        String normalized = normalizeUsername(username);
        if (normalized == null) {
            return;
        }
        redisTemplate.delete(failKey(normalized));
        redisTemplate.delete(lockKey(normalized));
    }

    public CaptchaResponse generateCaptcha(String username) {
        String normalized = normalizeUsername(username);
        if (normalized == null) {
            throw new BusinessException("error.auth.username.required");
        }

        LoginSecurityPolicy policy = policyService.getPolicyEntity();
        String captchaCode = randomCaptchaCode(4);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String imageBase64 = buildCaptchaImageBase64(captchaCode);
        String value = normalized + "|" + captchaCode;
        redisTemplate.opsForValue().set(captchaKey(captchaId), value, Duration.ofSeconds(policy.getCaptchaExpireSeconds()));
        return new CaptchaResponse(captchaId, imageBase64, policy.getCaptchaExpireSeconds());
    }

    private void validateCaptcha(String username, String captchaId, String captchaCode) {
        String normalized = normalizeUsername(username);
        if (normalized == null) {
            throw new BusinessException("error.auth.username.required");
        }
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            throw new BusinessException("error.auth.captcha.required");
        }
        String key = captchaKey(captchaId.trim());
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isBlank()) {
            throw new BusinessException("error.auth.captcha.expired");
        }
        String[] parts = value.split("\\|", 2);
        if (parts.length < 2) {
            redisTemplate.delete(key);
            throw new BusinessException("error.auth.captcha.expired");
        }
        String expectedUsername = parts[0];
        String expectedCode = parts[1];
        if (!normalized.equals(expectedUsername) || !expectedCode.equalsIgnoreCase(captchaCode.trim())) {
            redisTemplate.delete(key);
            throw new BusinessException("error.auth.captcha.invalid");
        }
        redisTemplate.delete(key);
    }

    private long getLockSeconds(String username) {
        Long ttl = redisTemplate.getExpire(lockKey(username));
        return ttl == null || ttl <= 0 ? 0 : ttl;
    }

    private long getFailCount(String username) {
        String value = redisTemplate.opsForValue().get(failKey(username));
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private long toLockMinutes(long lockSeconds) {
        if (lockSeconds <= 0) {
            return 0;
        }
        return (long) Math.ceil(lockSeconds / 60.0);
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        String normalized = username.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private String failKey(String username) {
        return KEY_FAIL_PREFIX + username;
    }

    private String lockKey(String username) {
        return KEY_LOCK_PREFIX + username;
    }

    private String captchaKey(String captchaId) {
        return KEY_CAPTCHA_PREFIX + captchaId;
    }

    private String randomCaptchaCode(int length) {
        char[] chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length);
            builder.append(chars[index]);
        }
        return builder.toString();
    }

    private String buildCaptchaImageBase64(String code) {
        int width = 132;
        int height = 42;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);

            for (int i = 0; i < 10; i++) {
                graphics.setColor(randomColor(120, 220));
                int x1 = ThreadLocalRandom.current().nextInt(width);
                int y1 = ThreadLocalRandom.current().nextInt(height);
                int x2 = ThreadLocalRandom.current().nextInt(width);
                int y2 = ThreadLocalRandom.current().nextInt(height);
                graphics.drawLine(x1, y1, x2, y2);
            }

            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(randomColor(20, 140));
                int x = 18 + i * 26 + ThreadLocalRandom.current().nextInt(3);
                int y = 31 + ThreadLocalRandom.current().nextInt(6);
                graphics.drawString(String.valueOf(code.charAt(i)), x, y);
            }
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException ex) {
            throw new BusinessException("common.server.error");
        }
    }

    private Color randomColor(int min, int max) {
        int red = ThreadLocalRandom.current().nextInt(min, max);
        int green = ThreadLocalRandom.current().nextInt(min, max);
        int blue = ThreadLocalRandom.current().nextInt(min, max);
        return new Color(red, green, blue);
    }
}
