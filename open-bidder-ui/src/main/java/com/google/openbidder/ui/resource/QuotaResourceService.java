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

package com.google.openbidder.ui.resource;

import com.google.api.services.compute.model.Quota;
import com.google.openbidder.ui.resource.model.QuotaResource;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;

import java.util.List;

/**
 * {@link ResourceService} for {@link QuotaResource}.
 */
public interface QuotaResourceService extends ResourceService<QuotaResource> {

  /**
   * @return {@link List} of {@link QuotaResource}s
   * from the {@code quotas} list.
   */
  List<QuotaResource> filterAndBuild(
      ResourceCollectionId resourceCollectionId,
      List<Quota> quotas);
}
