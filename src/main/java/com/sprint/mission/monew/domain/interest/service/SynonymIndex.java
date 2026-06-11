package com.sprint.mission.monew.domain.interest.service;

import com.sprint.mission.monew.common.config.SynonymProperties;
import com.sprint.mission.monew.common.util.JamoNormalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SynonymIndex {

  private final Map<String, Integer> suffixIndex;
  private final Map<String, Integer> prefixIndex;

  public SynonymIndex(SynonymProperties props) {
    this.suffixIndex = buildIndex(props.suffixGroups());
    this.prefixIndex = buildIndex(props.prefixGroups());
  }

  public Integer suffixGroupOf(String normalizedWord) {
    return suffixIndex.get(normalizedWord);
  }

  public Integer prefixGroupOf(String normalizedWord) {
    return prefixIndex.get(normalizedWord);
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