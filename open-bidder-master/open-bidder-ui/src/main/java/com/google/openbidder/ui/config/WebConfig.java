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

package com.google.openbidder.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Using this instead of {@code }&lt;mvc:annotation-driven&gt;} to configure the
 * {@link RequestMappingHandlerMapping#useSuffixPatternMatch} to {@code false}.
 * Otherwise {@link org.springframework.web.bind.annotation.PathVariable}s on the trailing end
 * of the URL are truncated at a period, which is super-annoying.
 * <p>
 * See https://jira.springsource.org/browse/SPR-6164
 * <p>
 * Note: us of Java-based configuration in Spring requires the CGLib library, which is not
 * officially supported on AppEngine. It seems the features actually used don't use any
 * restricted code or APIs however.
 */
@EnableWebMvc
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

  @Override
  @Bean
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    RequestMappingHandlerMapping hm = super.requestMappingHandlerMapping();
    hm.setUseSuffixPatternMatch(false);
    return hm;
  }
}
