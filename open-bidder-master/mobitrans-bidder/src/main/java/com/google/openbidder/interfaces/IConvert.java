/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.google.openbidder.interfaces;

/**
 *
 * @author tkhalilov
 */
public interface IConvert<T,S> {
   S Convert(T From);
}
