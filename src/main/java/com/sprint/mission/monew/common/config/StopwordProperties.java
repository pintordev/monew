package com.sprint.mission.monew.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monew.stopwords")
public record StopwordProperties(List<String> suffix, List<String> prefix) {}