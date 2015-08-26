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

package com.google.openbidder.util;

import com.google.common.base.Function;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * General reflection utilities.
 */
public class ReflectionUtils {
  private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

  public static final Function<Object, String> TO_SIMPLECLASSNAME = new Function<Object, String>() {
    @Override public String apply(@Nullable Object obj) {
      return obj == null ? "null" : getSimpleName(obj.getClass());
    }
  };

  public static final Function<Object, String> TO_CLASSNAME = new Function<Object, String>() {
    @Override public String apply(@Nullable Object obj) {
      return obj == null ? "null" : obj.getClass().getName();
    }
  };

  public static final Function<Object, String> TO_PRETTYCLASSNAME = new Function<Object, String>() {
    @Override public String apply(@Nullable Object obj) {
      return obj == null ? "null" : getPrettyName(obj.getClass());
    }
  };

  public static String getSimpleName(Class<?> klass) {
    String simpleName = klass.getSimpleName();

    if (Strings.isNullOrEmpty(simpleName)) {
      simpleName = klass.getName();
      simpleName = simpleName.substring(simpleName.lastIndexOf('.') + 1);
    }

    return simpleName;
  }

  public static String getPrettyName(Class<?> klass) {
    String canonName = klass.getCanonicalName();
    return canonName == null ? klass.getName() : canonName;
  }

  /**
   * Finds the single method in a class, that implements the desired annotation.
   */
  public static @Nullable Method findMethod(Class<?> klass, Class<? extends Annotation> annotation,
      Class<?>... signature) {
    for (Class<?> lookup = klass; lookup != null; lookup = lookup.getSuperclass()) {
      Method foundMethod = null;

      for (Method method : lookup.getDeclaredMethods()) {
        if (method.isAnnotationPresent(annotation)) {
          if (foundMethod != null) {
            logger.error("Class {} has more than one method annotated with {}",
                lookup.getSimpleName(), annotation.getSimpleName());
            return null;
          } else if (!Arrays.equals(signature, method.getParameterTypes())) {
            logger.error("Method {}.{} has incorrect signature {}, should be {}.",
                lookup.getSimpleName(), method.getName(), method.getParameterTypes(), signature);
            return null;
          } else {
            foundMethod = method;
          }
        }
      }

      if (foundMethod != null) {
        return foundMethod;
      }
    }

    return null;
  }

  /**
   * Invokes a method, wrapping any invocation exception as {@link IllegalArgumentException}.
   * Only {@link InvocationTargetException} is left alone for the caller to handle.
   */
  public static Object invoke(Method method, Object self)
      throws InvocationTargetException {
    try {
      return forceAccessible(method).invoke(self);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  public static Object readField(Class<?> klass, String name) {
    return readField(klass, null, name);
  }

  public static Object readField(Class<?> klass, @Nullable Object self, String name) {
    try {
      return forceAccessible(klass.getDeclaredField(name)).get(self);
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Force a class member to be accessible, even if it's not public. This will only work if
   * a security manager is not installed, or is installed but we have suppressAccessChecks policy.
   *
   * @param member a class member
   * @return the same as {@code member}
   * @throws SecurityException if the request is denied
   */
  public static <M extends Member> M forceAccessible(M member) {
    if ((member.getDeclaringClass().getModifiers() & Modifier.PUBLIC) == 0
        || (member.getModifiers() & Modifier.PUBLIC) == 0) {
      if (member instanceof AccessibleObject) {
        AccessibleObject accessibleMember = (AccessibleObject) member;

        if (!accessibleMember.isAccessible()) {
          accessibleMember.setAccessible(true);
        }
      }
    }

    return member;
  }

  public static void invokePostConstruct(List<?> objects) {
    for (Object object : objects) {
      Method method = findMethod(object.getClass(), PostConstruct.class);

      if (method != null) {
        try {
          invoke(method, object);
        } catch (InvocationTargetException e) {
          if (e.getCause() instanceof Error) {
            throw (Error) e.getCause();
          } else {
            throw new IllegalStateException(e.getCause());
          }
        }
      }
    }
  }

  public static void invokePreDestroy(List<?> objects) {
    for (Object object : objects) {
      Method method = findMethod(object.getClass(), PreDestroy.class);

      if (method != null) {
        try {
          invoke(method, object);
        } catch (InvocationTargetException e) {
          if (e.getCause() instanceof Error) {
            throw (Error) e.getCause();
          } else {
            logger.error("Failure destroying object {}: {}",
                object.getClass().getName(), e.getCause().getMessage());
          }
        }
      }
    }
  }
}
