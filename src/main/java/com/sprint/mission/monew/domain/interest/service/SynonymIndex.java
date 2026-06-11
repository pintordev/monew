package com.sprint.mission.monew.domain.interest.service;

import com.sprint.mission.monew.common.config.SynonymProperties;
import com.sprint.mission.monew.common.util.JamoNormalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SynonymIndex {

  public record SynonymMatch(String word, int groupIndex) {}

  private final Map<String, Integer> suffixIndex;
  private final Map<String, Integer> prefixIndex;

  public SynonymIndex(SynonymProperties props) {
    this.suffixIndex = buildIndex(props.suffixGroups());
    this.prefixIndex = buildIndex(props.prefixGroups());
  }

  public Optional<SynonymMatch> matchSuffix(String token) {
    for (Map.Entry<String, Integer> e : suffixIndex.entrySet()) {
      if (token.endsWith(e.getKey())) {
        return Optional.of(new SynonymMatch(e.getKey(), e.getValue()));
      }
    }
    return Optional.empty();
  }

  public Optional<SynonymMatch> matchPrefix(String token) {
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