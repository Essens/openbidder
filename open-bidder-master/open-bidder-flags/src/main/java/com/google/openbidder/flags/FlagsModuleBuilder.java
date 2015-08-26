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

package com.google.openbidder.flags;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.inject.Binder;
import com.google.inject.Module;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Special module that uses a two-stage binding method so parameters can be parsed from the
 * command line.
 * <p>
 * The modules are all passed to JCommander, which attempts to bind the command line to all
 * the associated {@code @Parameter} fields.
 * <p>
 * Second, those modules are installed into the same Binder. This way the
 * {@link Module#configure(Binder)} method of each module isn't called until the command
 * line parsing has occurred.
 */
public class FlagsModuleBuilder {
  private static final Logger logger = LoggerFactory.getLogger(FlagsModuleBuilder.class);

  private final List<Module> modules = new ArrayList<>();

  public final FlagsModuleBuilder addModule(Module module) {
    modules.add(checkNotNull(module));
    return this;
  }

  public final FlagsModuleBuilder addModules(Iterable<Module> modules) {
    Iterables.addAll(this.modules, modules);
    return this;
  }

  public final Module build(final String... args) {
    logger.info("Command line: {}", Joiner.on(" ").join(args));
    return new Module() {

      @Override
      public void configure(Binder binder) {
        JCommander jcommander = new JCommander();
        jcommander.addObject(modules);
        HelpFlag helpFlag = new HelpFlag();
        jcommander.addObject(helpFlag);
        ParameterException parameterException = null;
        try {
          jcommander.parse(args);
        } catch (ParameterException e) {
          logger.error("Error parsing command line", e);
          parameterException = e;
        }
        if (parameterException != null || helpFlag.help) {
          jcommander.usage();
          throw parameterException == null ? new HelpException() : parameterException;
        }
        for (Module module : modules) {
          binder.install(module);
        }
      }
    };
  }

  @Parameters
  private static class HelpFlag {
    @Parameter(description = "Help", names = "--help", help = true)
    private boolean help;
  }

  public static class HelpException extends RuntimeException {
    public HelpException() {
      super("Help executed");
    }
  }
}
