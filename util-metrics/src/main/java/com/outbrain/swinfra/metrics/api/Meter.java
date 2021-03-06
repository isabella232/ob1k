package com.outbrain.swinfra.metrics.api;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * exponentially-weighted moving average throughput.
 *
 * @author Eran Harel
 */
public interface Meter {

  /**
   * Mark the occurrence of an event.
   */
  public void mark();

  /**
   * Mark the occurrence of a given number of events.
   *
   * @param n the number of events
   */
  public void mark(long n);
}
