package com.sprint.mission.monew.domain.interest.mapper;

import com.sprint.mission.monew.domain.interest.dto.InterestDto;
import com.sprint.mission.monew.domain.interest.entity.Interest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterestMapper {

  @Mapping(target = "subscribedByMe", constant = "false")
  InterestDto toResponse(Interest interest);
}
