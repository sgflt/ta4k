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
package org.ta4j.core.strategy.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.strategy.Rule;

public class JustOnceRuleTest {

  private JustOnceRule rule;


  @Before
  public void setUp() {
    this.rule = new JustOnceRule();
  }


  @Test
  public void isSatisfied() {
    assertTrue(this.rule.isSatisfied());
    assertFalse(this.rule.isSatisfied());
    assertFalse(this.rule.isSatisfied());
    assertFalse(this.rule.isSatisfied());
    assertFalse(this.rule.isSatisfied());
  }


  @Test
  public void isSatisfiedWithInnerSatisfiedRule() {
    final var rule = new JustOnceRule(BooleanRule.TRUE);
    assertTrue(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
  }


  @Test
  public void isNotSatisfiedWithInnerNonSatisfiedRule() {
    final var rule = new JustOnceRule(BooleanRule.FALSE);
    assertFalse(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
  }


  @Test
  public void isSatisfiedWithInnerRule() {
    final var rule = new JustOnceRule(new Rule() {
      private int counter;


      @Override
      public boolean isSatisfied() {
        return this.counter++ > 0;
      }
    });
    assertFalse(rule.isSatisfied());
    assertTrue(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
    assertFalse(rule.isSatisfied());
  }
}
