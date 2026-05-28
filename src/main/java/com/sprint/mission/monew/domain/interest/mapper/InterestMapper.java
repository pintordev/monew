package com.sprint.mission.monew.domain.interest.mapper;

import com.sprint.mission.monew.domain.interest.dto.InterestResponse;
import com.sprint.mission.monew.domain.interest.entity.Interest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterestMapper {

  @Mapping(target = "subscribedByMe", constant = "false")
  @Mapping(target = "keywords", expression = "java(interest.getKeywords().stream().map(k -> k.getKeyword()).collect(java.util.stream.Collectors.toList()))")
  InterestResponse toResponse(Interest interest);
}