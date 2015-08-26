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

package com.google.openbidder.ui.controller.support;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.ui.resource.ResourceService;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.util.validation.Create;
import com.google.openbidder.ui.util.validation.Update;
import com.google.openbidder.ui.util.web.WebUtils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Resource controller for {@link ExternalResource}s with a parent using a
 * {@link com.google.openbidder.ui.resource.ResourceService}.
 */
public class ResourceController<T extends ExternalResource>
    extends AbstractResourceController<T> {

  private final ResourceService<T> resourceService;

  public ResourceController(
      ResourceService<T> resourceService) {

    this.resourceService = checkNotNull(resourceService);
  }

  @RequestMapping(
      value = "/{childResourceName}",
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public T get(
      HttpServletRequest request,
      @PathVariable("parentResourceName") String parentResourceName,
      @PathVariable("childResourceName") String childResourceName) {

    return resourceService.get(
        parentResourceName,
        childResourceName,
        WebUtils.convertParameters(request));
  }

  @RequestMapping(
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public List<? extends T> list(
      HttpServletRequest request,
      @PathVariable("parentResourceName") String parentResourceName) {

    return resourceService.list(parentResourceName, WebUtils.convertParameters(request));
  }

  @RequestMapping(
      method = RequestMethod.POST,
      consumes = "application/json",
      produces = "application/json")
  @ResponseBody
  public T createFromJson(
      @PathVariable("parentResourceName") String parentResourceName,
      @RequestBody @Validated(Create.class) T newResource) {

    return resourceService.create(parentResourceName, newResource);
  }

  @RequestMapping(
      method = RequestMethod.POST,
      produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> createFromForm(
      HttpServletRequest request,
      @PathVariable("parentResourceName") String parentResourceName,
      @Validated(Create.class) T newResource,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return bindingErrorResponse(request, bindingResult);
    }
    return new ResponseEntity<>(
        resourceService.create(parentResourceName, newResource),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/{childResourceName}",
      method = RequestMethod.PUT,
      consumes = "application/json",
      produces = "application/json")
  @ResponseBody
  public T updateFromJson(
      @PathVariable("parentResourceName") String parentResourceName,
      @PathVariable("childResourceName") String childResourceName,
      @RequestBody @Validated(Update.class) T updatedResource) {

    return resourceService.update(parentResourceName, childResourceName, updatedResource);
  }

  @RequestMapping(
      value = "/{childResourceName}",
      method = RequestMethod.DELETE)
  @ResponseBody
  public void delete(
      @PathVariable("parentResourceName") String parentResourceName,
      @PathVariable("childResourceName") String childResourceName) {

    resourceService.delete(parentResourceName, childResourceName);
  }
}
