package org.ta4j.core.num;

/**
 * @author Lukáš Kvídera
 */
public final class NumFactoryProvider {
  private static NumFactory defaultNumFactory = DoubleNumFactory.getInstance();


  private NumFactoryProvider() {
    // utility class
  }


  public static NumFactory getDefaultNumFactory() {
    return defaultNumFactory;
  }


  public static void setDefaultNumFactory(final NumFactory numFactory) {
    defaultNumFactory = numFactory;
  }
}
