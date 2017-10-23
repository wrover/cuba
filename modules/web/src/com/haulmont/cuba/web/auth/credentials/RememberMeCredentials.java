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

public class RememberMeCredentials extends LoginPasswordCredentials {

    public RememberMeCredentials(String login, String rememberMeToken, Locale locale) {
        super(login, rememberMeToken, locale);
    }

    public String getRememberMeToken() {
        return password;
    }

    public void setRememberMeToken(String rememberMeToken) {
        this.password = rememberMeToken;
    }

}
