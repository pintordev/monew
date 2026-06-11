package com.sprint.mission.monew.domain.interest.service;

import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.common.util.JamoNormalizer;
import com.sprint.mission.monew.domain.interest.dto.InterestCreateRequest;
import com.sprint.mission.monew.domain.interest.dto.InterestQueryCondition;
import com.sprint.mission.monew.domain.interest.dto.InterestResponse;
import com.sprint.mission.monew.domain.interest.dto.InterestUpdateRequest;
import com.sprint.mission.monew.domain.interest.entity.Interest;
import com.sprint.mission.monew.domain.interest.exception.InterestAlreadyExistsException;
import com.sprint.mission.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.mission.monew.domain.interest.mapper.InterestMapper;
import com.sprint.mission.monew.domain.interest.repository.InterestRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InterestService {

  private final InterestRepository interestRepository;
  private final InterestMapper interestMapper;
  private final SynonymIndex synonymIndex;

  public CursorPageResponse<InterestResponse> findAll(InterestQueryCondition condition,
      UUID userId) {
    return interestRepository.findInterests(condition, userId);
  }

  @Transactional
  public InterestResponse create(InterestCreateRequest request) {
    String name = request.name();
    log.debug("관심사 생성 시작 | name={}", name);

    if (interestRepository.existsByName(name)) {
      throw InterestAlreadyExistsException.withName(name);
    }

    String normalized = JamoNormalizer.normalize(name);
    int jamoLen = normalized.length();
    int minJamo = (int) Math.ceil(jamoLen * 0.8);
    int maxJamo = (int) Math.floor(jamoLen / 0.8);

    boolean typoMatch = interestRepository.findTypoCandidates(minJamo, maxJamo)
        .stream()
        .anyMatch(existing -> levenshteinSimilarity(normalized, JamoNormalizer.normalize(existing)) >= 0.8);

    List<String> rawTokens = splitRaw(name);
    List<String> normTokens = rawTokens.stream().map(JamoNormalizer::normalize).toList();
    boolean synonymMatch = !rawTokens.isEmpty() &&
        interestRepository.findNamesByTokens(rawTokens)
            .stream()
            .anyMatch(existing -> {
              List<String> existingTokens = splitRaw(existing).stream()
                  .map(JamoNormalizer::normalize).toList();
              return jaccardSimilarity(normTokens, existingTokens) >= 0.8;
            });

    if (typoMatch || synonymMatch) {
      throw InterestAlreadyExistsException.withName(name);
    }

    Interest saved = interestRepository.save(Interest.create(name, jamoLen, request.keywords()));
    log.info("관심사 생성 완료 | interestId={}, name={}", saved.getId(), saved.getName());
    return interestMapper.toResponse(saved);
  }

  @Transactional
  public InterestResponse updateKeywords(UUID id, InterestUpdateRequest request) {
    log.debug("관심사 키워드 수정 시작 | interestId={}", id);
    Interest interest = interestRepository.findById(id)
        .orElseThrow(() -> InterestNotFoundException.withId(id));
    interest.updateKeywords(request.keywords());
    log.info("관심사 키워드 수정 완료 | interestId={}", id);
    return interestMapper.toResponse(interest);
  }

  @Transactional
  public void hardDelete(UUID id) {
    log.debug("관심사 물리 삭제 시작 | interestId={}", id);
    Interest interest = interestRepository.findById(id)
        .orElseThrow(() -> InterestNotFoundException.withId(id));
    interestRepository.delete(interest);
    log.info("관심사 물리 삭제 완료 | interestId={}", id);
  }

  private List<String> splitRaw(String s) {
    return Arrays.stream(s.trim().split("\\s+"))
        .filter(t -> !t.isBlank())
        .toList();
  }

  private String canonicalize(String token) {
    return synonymIndex.matchSuffix(token)
        .map(m -> {
          String core = token.substring(0, token.length() - m.word().length());
          return stripAnyPrefix(core) + "_S" + m.groupIndex();
        })
        .orElseGet(() -> synonymIndex.matchPrefix(token)
            .map(m -> "P" + m.groupIndex() + "_" + token.substring(m.word().length()))
            .orElse(token));
  }

  private String stripAnyPrefix(String core) {
    return synonymIndex.matchPrefix(core)
        .map(m -> core.substring(m.word().length()))
        .orElse(core);
  }

  private double jaccardSimilarity(List<String> tokensA, List<String> tokensB) {
    Set<String> a = tokensA.stream().map(this::canonicalize).collect(Collectors.toSet());
    Set<String> b = tokensB.stream().map(this::canonicalize).collect(Collectors.toSet());
    long intersection = a.stream().filter(b::contains).count();
    long union = a.size() + b.size() - intersection;
    return union == 0 ? 1.0 : (double) intersection / union;
  }

  private double levenshteinSimilarity(String a, String b) {
    int maxLen = Math.max(a.length(), b.length());
    if (maxLen == 0) {
      return 1.0;
    }
    return 1.0 - (double) levenshtein(a, b) / maxLen;
  }

  private int levenshtein(String a, String b) {
    int[] prev = new int[b.length() + 1];
    for (int j = 0; j <= b.length(); j++) {
      prev[j] = j;
    }
    for (int i = 1; i <= a.length(); i++) {
      int[] curr = new int[b.length() + 1];
      curr[0] = i;
      for (int j = 1; j <= b.length(); j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
      }
      prev = curr;
    }
    return prev[b.length()];
  }
}