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

import com.google.common.base.Preconditions;
import com.google.openbidder.ui.resource.RootResourceService;
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
 * Resource controller for root-level {@link ExternalResource}s using a
 * {@link com.google.openbidder.ui.resource.RootResourceService}.
 */
public class RootResourceController<T extends ExternalResource>
    extends AbstractResourceController<T> {

  private final RootResourceService<T> rootResourceService;

  public RootResourceController(RootResourceService<T> rootResourceService) {
    this.rootResourceService = Preconditions.checkNotNull(rootResourceService);
  }

  @RequestMapping(
      value = "/{resourceName}",
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public T get(
      HttpServletRequest request,
      @PathVariable("resourceName") String resourceName) {

    return rootResourceService.get(resourceName, WebUtils.convertParameters(request));
  }

  @RequestMapping(
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public List<? extends T> list(HttpServletRequest request) {
    return rootResourceService.list(WebUtils.convertParameters(request));
  }

  @RequestMapping(
      method = RequestMethod.POST,
      consumes = "application/json",
      produces = "application/json")
  @ResponseBody
  public T createFromJson(@Validated(Create.class) @RequestBody T newResource) {
    return rootResourceService.create(newResource);
  }

  @RequestMapping(
      method = RequestMethod.POST,
      produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> createFromForm(
      HttpServletRequest request,
      @Validated(Create.class) T newResource,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return bindingErrorResponse(request, bindingResult);
    }
    return new ResponseEntity<>(rootResourceService.create(newResource), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/{resourceName}",
      method = RequestMethod.PUT,
      consumes = "application/json",
      produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> update(
      HttpServletRequest request,
      @PathVariable("resourceName") String resourceName,
      @Validated(Update.class) @RequestBody T updatedResource) {

    return new ResponseEntity<>(
        rootResourceService.update(resourceName, updatedResource),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/{resourceName}",
      method = RequestMethod.DELETE)
  @ResponseBody
  public void delete(@PathVariable("resourceName") String resourceName) {
    rootResourceService.delete(resourceName);
  }
}
