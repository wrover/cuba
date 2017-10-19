/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.web.auth.provider;

import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.auth.AuthInfo;

/**
 * LoginProvider analyses user's input from a login screen and decides
 *  whether it can authenticate the user.
 */
public interface LoginProvider {

    /**
     * Defines the highest precedence for {@link org.springframework.core.Ordered} providers of the platform.
     */
    int HIGHEST_PLATFORM_PRECEDENCE = 100;

    /**
     * Defines the lowest precedence for {@link org.springframework.core.Ordered} providers of the platform.
     */
    int LOWEST_PLATFORM_PRECEDENCE = 1000;

    /**
     * Process user's input and authenticates the user if possible
     *
     * @param authenticated        is the user already authenticated
     * @param authInfo          input provided by the user
     * @return                  whether the user is authenticates
     * @throws LoginException   if the input provided by the user is incorrect
     */
    boolean process(boolean authenticated, AuthInfo authInfo) throws LoginException;

}
