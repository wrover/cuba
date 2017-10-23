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

import com.haulmont.cuba.security.global.UserSession;

public class AuthenticationStatus {

    protected boolean success;

    protected UserSession session;

    protected AuthenticationStatus(boolean success, UserSession session) {
        this.success = success;
        this.session = session;
    }

    public boolean isSuccess() {
        return success;
    }

    public UserSession getSession() {
        return session;
    }

    public static AuthenticationStatus notSuccessful() {
        return new AuthenticationStatus(false, null);
    }

    public static AuthenticationStatus successful(UserSession session) {
        return new AuthenticationStatus(true, session);
    }
}
