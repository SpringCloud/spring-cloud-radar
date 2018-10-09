package com.ppdai.infrastructure.radar;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.ppdai.infrastructure.rest.RestApplication;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { RestApplication.class})
@ActiveProfiles("TEST")
@Rollback
abstract public class AbstractTest {

}
