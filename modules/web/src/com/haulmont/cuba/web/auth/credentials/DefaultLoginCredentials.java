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

package com.haulmont.cuba.web.auth.credentials;

import java.util.Locale;

public class DefaultLoginCredentials implements LoginCredentials {

    private final String login;
    private final String password;
    private final Boolean rememberMe;
    private final Locale locale;

    public DefaultLoginCredentials(String login, String password, Boolean rememberMe, Locale locale) {
        this.login = login;
        this.password = password;
        this.rememberMe = rememberMe;
        this.locale = locale;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public Locale getLocale() {
        return locale;
    }

}
