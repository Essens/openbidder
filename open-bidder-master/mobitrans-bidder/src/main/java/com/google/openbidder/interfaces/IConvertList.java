/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.google.openbidder.interfaces;

import java.util.List;

/**
 *
 * @author tkhalilov
 */
public interface IConvertList<T,S> {
    Iterable<T> Convert(Iterable<S> From);
}
