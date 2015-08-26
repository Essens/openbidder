/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.google.openbidder.cache;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author tkhalilov
 */
public class CounterCache {
    public static AtomicInteger RequestsPerSecondCounter = new AtomicInteger(0);
    public static AtomicInteger BidsPerSecondCounter = new AtomicInteger(0);
}
