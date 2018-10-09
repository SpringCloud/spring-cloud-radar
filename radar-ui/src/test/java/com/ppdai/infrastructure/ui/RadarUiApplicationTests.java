package com.ppdai.infrastructure.ui;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RadarUiApplicationTests {

	@Autowired
	private SoaConfig soaConfig;
	@Autowired
	private Environment environment;	

}
