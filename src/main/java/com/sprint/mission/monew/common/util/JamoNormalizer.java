package com.sprint.mission.monew.common.util;

import java.text.Normalizer;

public class JamoNormalizer {

  private JamoNormalizer() {}

  public static String normalize(String s) {
    return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
        .replaceAll("\\s+", "");
  }
}