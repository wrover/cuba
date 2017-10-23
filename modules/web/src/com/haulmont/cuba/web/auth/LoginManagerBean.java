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

package com.haulmont.cuba.web.auth;

import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.auth.credentials.LocalizedCredentials;
import com.haulmont.cuba.web.auth.credentials.LoginCredentials;
import com.haulmont.cuba.web.auth.provider.AbstractLoginProvider;
import com.haulmont.cuba.web.auth.provider.LoginProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Locale;

@Component(LoginManager.NAME)
public class LoginManagerBean implements LoginManager {

    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected GlobalConfig globalConfig;
    @Inject
    protected Messages messages;

    @Inject
    protected List<LoginProvider> loginProviders;

    @PostConstruct
    public void init() {
        for (int i = 0; i < loginProviders.size(); ++i) {
            if (i != loginProviders.size() - 1) {
                ((AbstractLoginProvider) loginProviders.get(i)).setNextLoginProvider(loginProviders.get(i + 1));
            }
        }
    }

    @Override
    public void login(LoginCredentials credentials) throws LoginException {

//        checkParameters(credentials);

        if (credentials instanceof LocalizedCredentials) {
            App.getInstance().setLocale(((LocalizedCredentials) credentials).getLocale());
        }

        boolean authenticated = getFirstProvider().process(false, credentials);

        if (authenticated) {
            // locale could be set on the server
            Locale loggedInLocale = userSessionSource.getLocale();

            if (globalConfig.getLocaleSelectVisible()) {
                App.getInstance().addCookie(App.COOKIE_LOCALE, loggedInLocale.toLanguageTag());
            }
        } else {
            throw new IllegalStateException("Credentials have passed through all providers but " +
                    "the user wasn't authenticated.");
        }
    }

//    protected void checkParameters(AuthInfo authInfo) throws LoginException {
//        if (StringUtils.isEmpty(authInfo.getLogin()) || StringUtils.isEmpty(authInfo.getPassword())) {
//            throw new LoginException(messages.getMainMessage("loginWindow.emptyLoginOrPassword"));
//        }
//    }

    protected LoginProvider getFirstProvider() {
        return loginProviders.get(0);
    }

}
