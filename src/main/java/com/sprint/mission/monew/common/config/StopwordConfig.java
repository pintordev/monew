package com.sprint.mission.monew.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StopwordProperties.class)
public class StopwordConfig {}