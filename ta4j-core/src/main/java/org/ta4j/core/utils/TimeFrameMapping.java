package org.ta4j.core.utils;

import java.time.Duration;
import java.util.Map;

import lombok.experimental.UtilityClass;
import org.ta4j.core.indicators.TimeFrame;

/**
 * @author Lukáš Kvídera
 */
@UtilityClass
public class TimeFrameMapping {
  private static final Map<TimeFrame, Duration> timeframeToDurationMapping = Map.ofEntries(
      Map.entry(TimeFrame.MINUTES_1, Duration.ofSeconds(60)),
      Map.entry(TimeFrame.MINUTES_5, Duration.ofSeconds(5L * 60)),
      Map.entry(TimeFrame.MINUTES_15, Duration.ofSeconds(15L * 60)),
      Map.entry(TimeFrame.MINUTES_30, Duration.ofSeconds(30L * 60)),
      Map.entry(TimeFrame.HOURS_1, Duration.ofSeconds(60L * 60)),
      Map.entry(TimeFrame.HOURS_4, Duration.ofSeconds(4L * 60 * 60)),
      Map.entry(TimeFrame.DAY, Duration.ofSeconds(24L * 60 * 60)),
      Map.entry(TimeFrame.WEEK, Duration.ofSeconds(7 * 24L * 60 * 60)),
      Map.entry(TimeFrame.MONTH, Duration.ofSeconds(30 * 24L * 60 * 60))
  );


  public static Duration getDuration(final TimeFrame resolution) {
    return timeframeToDurationMapping.get(resolution);
  }
}
