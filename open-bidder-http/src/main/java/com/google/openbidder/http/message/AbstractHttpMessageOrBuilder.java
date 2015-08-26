/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.http.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Base implementation of {@link HttpMessageOrBuilder}.
 */
public abstract class AbstractHttpMessageOrBuilder implements HttpMessageOrBuilder {
  private ContentHolder contentHolder;

  @Nullable
  @Override
  public final MediaType getMediaType() {
    Collection<String> contentType = getHeaders().get(HttpHeaders.CONTENT_TYPE);
    return contentType.isEmpty()
        ? null
        : MediaType.parse(Iterables.getOnlyElement(contentType, null));
  }

  protected final ContentHolder contentHolder() {
    if (contentHolder == null) {
      contentHolder = newContentHolder();
    }
    return contentHolder;
  }

  protected final void setContentHolder(@Nullable ContentHolder contentHolder) {
    this.contentHolder = contentHolder;
  }

  protected ContentHolder newContentHolder() {
    return new ContentHolder(this, -1, ContentHolder.State.NONE);
  }

  protected final @Nullable ContentHolder closeContentHolder() {
    ContentHolder ret = contentHolder;
    if (ret != null) {
      ret.outputClose();
      contentHolder = null;
    }
    return ret;
  }

  @Override
  public final String toString() {
    return toStringHelper().toString();
  }

  protected MoreObjects.ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("headers", getHeaders().isEmpty() ? null : getHeaders())
        .add("cookies", getCookies().isEmpty() ? null : getCookies());
  }
}
