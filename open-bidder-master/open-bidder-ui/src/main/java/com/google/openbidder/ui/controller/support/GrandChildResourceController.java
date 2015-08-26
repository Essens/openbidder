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

package com.google.openbidder.ui.controller.support;

import com.google.common.base.Preconditions;
import com.google.openbidder.ui.resource.GrandChildResourceService;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.util.validation.Create;
import com.google.openbidder.ui.util.web.WebUtils;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Resource controller for {@link ExternalResource}s with a zone parent using a
 * {@link com.google.openbidder.ui.resource.GrandChildResourceService}.
 */
public class GrandChildResourceController<T extends ExternalResource>
    extends AbstractResourceController<T>  {

  private final GrandChildResourceService<T> resourceService;

  public GrandChildResourceController(GrandChildResourceService<T> resourceService) {
    this.resourceService = Preconditions.checkNotNull(resourceService);
  }

  @RequestMapping(
      value = "/{grandChildResourceName}",
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public T get(
      HttpServletRequest request,
      @PathVariable("parentResourceName") String parentResourceName,
      @PathVariable("childResourceName") String childResourceName,
      @PathVariable("grandChildResourceName") String grandChildResourceName) {

    return resourceService.get(
        parentResourceName,
        childResourceName,
        grandChildResourceName,
        WebUtils.convertParameters(request));
  }

  @RequestMapping(
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public List<? extends T> list(
      HttpServletRequest request,
      @PathVariable("parentResourceName") String parentResourceName,
      @PathVariable("childResourceName") String childResourceName) {

    return resourceService.list(
        parentResourceName,
        childResourceName,
        WebUtils.convertParameters(request));
  }

  @RequestMapping(
      method = RequestMethod.POST,
      consumes = "application/json",
      produces = "application/json")
  @ResponseBody
  public T createFromJson(
      @PathVariable("parentResourceName") String parentResourceName,
      @PathVariable("childResourceName") String childResourceName,
      @RequestBody @Validated(Create.class) T newResource) {

    return resourceService.create(parentResourceName, childResourceName, newResource);
  }

  @RequestMapping(
      value = "/{grandChildResourceName}",
      method = RequestMethod.DELETE)
  @ResponseBody
  public void delete(
      @PathVariable("parentResourceName") String parentResourceName,
      @PathVariable("childResourceName") String childResourceName,
      @PathVariable("grandChildResourceName") String grandChildResourceName) {

    resourceService.delete(parentResourceName, childResourceName, grandChildResourceName);
  }
}
