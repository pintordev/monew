package com.sprint.mission.monew.domain.interest.util;

import com.sprint.mission.monew.common.config.SynonymProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SynonymUtils {

  private record SynonymMatch(String word, int groupIndex) {}

  private final Map<String, Integer> suffixIndex;
  private final Map<String, Integer> prefixIndex;

  public SynonymUtils(SynonymProperties props) {
    this.suffixIndex = buildIndex(props.suffixGroups());
    this.prefixIndex = buildIndex(props.prefixGroups());
  }

  public double jaccardSimilarity(List<String> tokensA, List<String> tokensB) {
    Set<String> a = tokensA.stream().map(this::canonicalize).collect(Collectors.toSet());
    Set<String> b = tokensB.stream().map(this::canonicalize).collect(Collectors.toSet());
    long intersection = a.stream().filter(b::contains).count();
    long union = a.size() + b.size() - intersection;
    return union == 0 ? 1.0 : (double) intersection / union;
  }

  private String canonicalize(String token) {
    return matchSuffix(token)
        .map(m -> {
          String core = token.substring(0, token.length() - m.word().length());
          return stripAnyPrefix(core) + "_S" + m.groupIndex();
        })
        .orElseGet(() -> matchPrefix(token)
            .map(m -> "P" + m.groupIndex() + "_" + token.substring(m.word().length()))
            .orElse(token));
  }

  private String stripAnyPrefix(String core) {
    return matchPrefix(core)
        .map(m -> core.substring(m.word().length()))
        .orElse(core);
  }

  private Optional<SynonymMatch> matchSuffix(String token) {
    for (Map.Entry<String, Integer> e : suffixIndex.entrySet()) {
      if (token.endsWith(e.getKey())) {
        return Optional.of(new SynonymMatch(e.getKey(), e.getValue()));
      }
    }
    return Optional.empty();
  }

  private Optional<SynonymMatch> matchPrefix(String token) {
    for (Map.Entry<String, Integer> e : prefixIndex.entrySet()) {
      String word = e.getKey();
      if (token.startsWith(word) && token.length() > word.length()) {
        return Optional.of(new SynonymMatch(word, e.getValue()));
      }
    }
    return Optional.empty();
  }

  private static Map<String, Integer> buildIndex(List<Set<String>> groups) {
    Map<String, Integer> index = new HashMap<>();
    for (int i = 0; i < groups.size(); i++) {
      for (String word : groups.get(i)) {
        index.put(JamoNormalizer.normalize(word), i);
      }
    }
    return Map.copyOf(index);
  }
}