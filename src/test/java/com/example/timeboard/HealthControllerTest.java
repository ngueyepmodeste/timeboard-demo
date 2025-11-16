package com.example.timeboard;

import com.example.timeboard.controller.HealthController;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class HealthControllerTest {

    @Test
    void healthShouldReturnOk() {
        HealthController controller = new HealthController();
        assertThat(controller.health().get("status")).isEqualTo("OK");
    }
}
