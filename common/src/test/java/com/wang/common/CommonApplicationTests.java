package com.wang.common;

import com.wang.common.untils.Argon2idUtil;
import org.junit.jupiter.api.Test;

/**
 * 密码生成工具测试类
 */
class CommonApplicationTests {

    /**
     * 生成测试用户密码（123456）
     * 运行此测试生成 Argon2id 加密的密码哈希值
     */
    @Test
    void generatePasswordHash() {
        String password = "123456";
        System.out.println("========== 生成10个密码哈希值（密码: 123456）==========");
        for (int i = 1; i <= 10; i++) {
            String hash = Argon2idUtil.hash(password);
            System.out.println("用户" + i + ": " + hash);
        }
        System.out.println("=================================================");
    }

    /**
     * 验证密码是否正确
     */
    @Test
    void verifyPassword() {
        // 替换为实际的哈希值进行验证
        String hash = "$argon2id$v=19$m=65536,t=2,p=1$xxxxxxxx";
        String password = "123456";
        boolean result = Argon2idUtil.verify(hash, password);
        System.out.println("验证结果: " + result);
    }
}
