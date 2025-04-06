package org.ta4j.core.num;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * @author Lukáš Kvídera
 */
@UtilityClass
public final class NumFactoryProvider {
  @Getter
  @Setter
  private static NumFactory defaultNumFactory = DoubleNumFactory.getInstance();
}
