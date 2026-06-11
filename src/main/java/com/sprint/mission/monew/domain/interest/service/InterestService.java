package com.sprint.mission.monew.domain.interest.service;

import com.sprint.mission.monew.common.config.StopwordProperties;
import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.domain.interest.dto.InterestCreateRequest;
import com.sprint.mission.monew.domain.interest.dto.InterestQueryCondition;
import com.sprint.mission.monew.domain.interest.dto.InterestResponse;
import com.sprint.mission.monew.domain.interest.dto.InterestUpdateRequest;
import com.sprint.mission.monew.domain.interest.entity.Interest;
import com.sprint.mission.monew.domain.interest.exception.InterestAlreadyExistsException;
import com.sprint.mission.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.mission.monew.domain.interest.mapper.InterestMapper;
import com.sprint.mission.monew.domain.interest.repository.InterestRepository;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
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
  private final StopwordProperties stopwordProperties;

  public CursorPageResponse<InterestResponse> findAll(InterestQueryCondition condition, UUID userId) {
    return interestRepository.findInterests(condition, userId);
  }

  @Transactional
  public InterestResponse create(InterestCreateRequest request) {
    String name = request.name();
    log.debug("관심사 생성 시작 | name={}", name);

    if (interestRepository.existsByName(name)) {
      throw InterestAlreadyExistsException.withName(name);
    }

    String normalized = normalize(name);
    int jamoLen = normalized.length();
    int minJamo = (int) Math.ceil(jamoLen * 0.8);
    int maxJamo = (int) Math.floor(jamoLen / 0.8);

    boolean typoMatch = interestRepository.findTypoCandidates(minJamo, maxJamo)
        .stream()
        .anyMatch(existing -> levenshteinSimilarity(normalized, normalize(existing)) >= 0.8);

    List<String> tokens = tokenize(name);
    boolean synonymMatch = !tokens.isEmpty() &&
        interestRepository.findNamesByTokens(tokens)
            .stream()
            .anyMatch(existing -> jaccardSimilarity(tokens, tokenize(existing)) >= 0.8);

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

  private String normalize(String s) {
    return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
        .replaceAll("\\s+", "");
  }

  private List<String> tokenize(String s) {
    return Arrays.stream(s.trim().split("\\s+"))
        .map(this::normalize)
        .filter(t -> !t.isBlank())
        .toList();
  }

  private String stripSuffix(String token, List<String> suffixes) {
    for (String suffix : suffixes) {
      String ns = normalize(suffix);
      if (token.endsWith(ns) && token.length() > ns.length()) {
        return token.substring(0, token.length() - ns.length());
      }
    }
    return token;
  }

  private String stripPrefix(String token, List<String> prefixes) {
    for (String prefix : prefixes) {
      String np = normalize(prefix);
      if (token.startsWith(np) && token.length() > np.length()) {
        return token.substring(np.length());
      }
    }
    return token;
  }

  private double jaccardSimilarity(List<String> tokensA, List<String> tokensB) {
    List<String> a = tokensA.stream()
        .map(t -> stripSuffix(stripPrefix(t, stopwordProperties.prefix()), stopwordProperties.suffix()))
        .filter(t -> !t.isBlank())
        .toList();
    List<String> b = tokensB.stream()
        .map(t -> stripSuffix(stripPrefix(t, stopwordProperties.prefix()), stopwordProperties.suffix()))
        .filter(t -> !t.isBlank())
        .toList();

    if (a.isEmpty() || b.isEmpty()) {
      return levenshteinSimilarity(String.join("", tokensA), String.join("", tokensB));
    }

    Set<String> setA = new HashSet<>(a);
    Set<String> setB = new HashSet<>(b);
    long intersection = setA.stream().filter(setB::contains).count();
    long union = Stream.concat(setA.stream(), setB.stream()).distinct().count();
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