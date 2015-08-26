/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.openbidder.api.interceptor;

/**
 * An exception thrown when an interceptor decides that the processing of a request should
 * immediately halt, all interceptors up the stack should not do any further processing and an
 * empty response should be returned. For example, this may be thrown when a bid interceptor
 * determines that a bid request is undesirable for one reason or another, and wishes to ensure
 * that none of the following interceptors in the chain execute and no bids are placed.
 * <p>
 * This exception should not be thrown for short-term errors, or for errors that occur as
 * a result of a failure state of the bidder itself, for example if connectivity to an external
 * service is lost.  In this case another {@link RuntimeException} should be thrown, so
 * that the bidder returns a non-200 response, giving the load balancer a chance to route
 * future bid requests to other bidders.
 */
public class InterceptorAbortException extends RuntimeException {
  public InterceptorAbortException() {
  }

  public InterceptorAbortException(String m) {
    super(m);
  }

  public InterceptorAbortException(String m, Throwable t) {
    super(m, t);
  }

  public InterceptorAbortException(Throwable t) {
    super(t);
  }
}
