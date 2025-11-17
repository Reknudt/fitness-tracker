package com.pavlov.workout.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Locale;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc   // (printOnlyOnFailure = false)
@ActiveProfiles("test")
public abstract class AbstractWebTest {
    protected static final long ID_1 = 1L;
    protected static final long BAD_ID = -1L;
    protected static final Locale LOCALE_RU = Locale.of("ru");
    protected static final LocalDate NOW = LocalDate.now();

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
}

// https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3

// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-oauth2-test/src/main/java/com/c4_soft/springaddons/security/oauth2/test/annotations/WithMockJwtAuth.java
