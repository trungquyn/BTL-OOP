package com.cinema;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — kiểm tra Spring context load được không.
 *
 * Lưu ý: Test này yêu cầu MongoDB đang chạy.
 * Nếu không có MongoDB, test sẽ fail ở context loading.
 */
@SpringBootTest
class CinemaApplicationTests {

    @Test
    void contextLoads() {
        // Test này pass nếu Spring ApplicationContext load thành công
    }
}
