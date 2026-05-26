package com.sprint.mission.monew.domain.interest.controller.api;

import com.sprint.mission.monew.common.dto.ErrorResponse;
import com.sprint.mission.monew.domain.interest.dto.InterestCreateRequest;
import com.sprint.mission.monew.domain.interest.dto.InterestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Interest", description = "관심사 API")
public interface InterestApi {

  @Operation(summary = "관심사 등록", description = "새로운 관심사를 등록합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "등록 성공",
        content = @Content(schema = @Schema(implementation = InterestDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (입력값 검증 실패)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "유사 관심사 중복",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<InterestDto> create(
      @Valid @RequestBody InterestCreateRequest request,
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId);
}
