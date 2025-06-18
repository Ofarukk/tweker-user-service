package com.tweker.user;

import com.tweker.user.config.GlobalTestConfig;
import com.tweker.user.config.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@Import({GlobalTestConfig.class, PostgresTestContainerConfig.class})
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
