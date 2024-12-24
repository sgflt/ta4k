/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.api.strategy;

import java.util.function.BooleanSupplier;

import org.ta4j.core.rules.AndRule;
import org.ta4j.core.rules.BooleanRule;
import org.ta4j.core.rules.NotRule;
import org.ta4j.core.rules.OrRule;
import org.ta4j.core.rules.XorRule;

/**
 * A rule (also called "trading rule") used to build a {@link Strategy trading
 * strategy}. A trading rule can consist of a combination of other rules.
 */
public interface Rule {

  Rule NOOP = BooleanRule.FALSE;


  /**
   * @param rule another trading rule
   *
   * @return a rule which is the AND combination of this rule with the provided
   *     one
   */
  default Rule and(final Rule rule) {
    return new AndRule(this, rule);
  }

  /**
   * @param rule another trading rule
   *
   * @return a rule which is the OR combination of this rule with the provided one
   */
  default Rule or(final Rule rule) {
    return new OrRule(this, rule);
  }

  /**
   * @param rule another trading rule
   *
   * @return a rule which is the XOR combination of this rule with the provided
   *     one
   */
  default Rule xor(final Rule rule) {
    return new XorRule(this, rule);
  }

  /**
   * @return a rule which is the logical negation of this rule
   */
  default Rule negation() {
    return new NotRule(this);
  }

  /**
   * @return true if this rule is satisfied for the provided index, false
   *     otherwise
   */
  boolean isSatisfied();

  static Rule of(final BooleanSupplier rule) {
    return rule::getAsBoolean;
  }
}
