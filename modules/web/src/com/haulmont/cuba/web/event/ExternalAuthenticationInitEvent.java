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

package com.haulmont.cuba.web.event;

import com.haulmont.cuba.web.Connection;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

public class ExternalAuthenticationInitEvent extends ApplicationEvent {

    /**
     * Set this field to true if your handler authorized user
     */
    protected boolean authenticated;

    protected String userName;

    protected Locale locale;

    public ExternalAuthenticationInitEvent(Connection connection, String userName, Locale locale) {
        super(connection);
        this.userName = userName;
        this.locale = locale;
    }

    public Connection getConnection() {
        return (Connection) getSource();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUserName() {
        return userName;
    }

    public Locale getLocale() {
        return locale;
    }
}
