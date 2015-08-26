/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.openbidder.http.util;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The smallest possible, well-formed/portable, transparent 1x1 GIF image.
 */
public final class PixelImage {
  private static final byte[] PIXEL = new byte[]{
      'G','I','F','8','9','a', // Header
      0x01, 0x00,              // Width
      0x01, 0x00,              // Height
      (byte) 0xF0,             // Logical Screen Descriptor:
                               // GCT=1, Color res=7+1=8, Sort=0, GCT size=2^(0+1)=2
      0x01,                    // - Background = Color [1]
      0x00,                    // - Pixel aspect ratio = none
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, // - Color[0] (foreground)
      0x00, 0x00, 0x00,                      // - Color[1] (background)
      0x21, (byte) 0xF9,       // Graphic Control Extension:
      0x04,                    // - GCE size
      0x01,                    // - Have transparent background
      0x0A, 0x00,              // - Animation delay
      0x00,                    // - Color [0] is transparent
      0x00,                    // - End GCE
      0x2C,                    // Image Descriptor:
      0x00, 0x00,              // - Top pos
      0x00, 0x00,              // - Left pos
      0x01, 0x00,              // - Width
      0x01, 0x00,              // - Height
      0x00,                    // - No local color table
      0x02,                    // - Start of image: LZW minimum symbol size = 2 bits
      0x02,                    // - 2 bytes of LZW encoded data
      0x44, 0x01,              // - LZW data
      0x00,                    // - End of image data
      0x3B                     // GIF file terminator
  };

  public static ByteString PIXEL_BYTES = ByteString.copyFrom(PIXEL);

  private PixelImage() {
  }

  public static void write(OutputStream out) throws IOException {
    out.write(PIXEL);
  }
}
