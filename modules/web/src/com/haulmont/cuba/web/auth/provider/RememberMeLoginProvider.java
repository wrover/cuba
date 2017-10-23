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

import com.haulmont.bali.util.URLEncodeUtils;
import com.haulmont.cuba.security.app.UserManagementService;
import com.haulmont.cuba.security.auth.RememberMeCredentials;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.WebConfig;
import com.haulmont.cuba.web.auth.LoginCookies;
import com.haulmont.cuba.web.auth.credentials.LoginCredentials;
import com.haulmont.cuba.web.auth.credentials.RememberMeLoginPasswordCredentials;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * {@link LoginProvider} that checks if the user tries to authenticate by "Remember me" functionality
 */
@Component("cuba_RememberMeProvider")
public class RememberMeLoginProvider extends AbstractLoginProvider implements Ordered {

    @Inject
    protected UserManagementService userManagementService;
    @Inject
    protected WebConfig webConfig;

    @Override
    protected boolean tryToAuthenticate(LoginCredentials credentials) throws LoginException {

        boolean result = false;

        if (credentials instanceof RememberMeLoginPasswordCredentials) {
            RememberMeLoginPasswordCredentials rememberMeCredentials = (RememberMeLoginPasswordCredentials) credentials;

            if (rememberMeCredentials.isRememberMe()) {
                getConnection().login(
                        new RememberMeCredentials(
                                rememberMeCredentials.getLogin(),
                                rememberMeCredentials.getPassword(),
                                getLocale(credentials)
                        )
                );

                result = true;
            }
        }

        return result;
    }

    /**
     * Checks if the "Remember me" checkbox was turned on or off and edits cookies based on it
     *
     * @param credentials  input provided by the user
     */
    @Override
    protected void afterAll(boolean authenticated, LoginCredentials credentials) {
        super.afterAll(authenticated, credentials);

        if (credentials instanceof RememberMeLoginPasswordCredentials) {

            RememberMeLoginPasswordCredentials rememberMeCredentials = (RememberMeLoginPasswordCredentials) credentials;

            if (Boolean.TRUE.equals(rememberMeCredentials.isRememberMe())) {
                if (!isRememberMeUsed(rememberMeCredentials)) {
                    getApp().addCookie(LoginCookies.COOKIE_REMEMBER_ME_USED, Boolean.TRUE.toString());

                    String encodedLogin = URLEncodeUtils.encodeUtf8(rememberMeCredentials.getLogin());

                    getApp().addCookie(LoginCookies.COOKIE_REMEMBER_ME_LOGIN, StringEscapeUtils.escapeJava(encodedLogin));

                    UserSession session = getConnection().getSession();
                    if (session == null) {
                        throw new IllegalStateException("Unable to get session after login");
                    }

                    User user = session.getUser();

                    String rememberMeToken = userManagementService.generateRememberMeToken(user.getId());

                    getApp().addCookie(LoginCookies.COOKIE_REMEMBER_ME_PASSWORD, rememberMeToken);
                }
            } else {
                getApp().removeCookie(LoginCookies.COOKIE_REMEMBER_ME_USED);
                getApp().removeCookie(LoginCookies.COOKIE_REMEMBER_ME_LOGIN);
                getApp().removeCookie(LoginCookies.COOKIE_REMEMBER_ME_PASSWORD);
            }
        }
    }

    private boolean isRememberMeUsed(RememberMeLoginPasswordCredentials rememberMeCredentials) {
        boolean result = false;

        if (webConfig.getRememberMeEnabled()) {
            String rememberMeCookie = getApp().getCookieValue(LoginCookies.COOKIE_REMEMBER_ME_USED);
            if (Boolean.parseBoolean(rememberMeCookie)) {
                String encodedLogin = getApp().getCookieValue(LoginCookies.COOKIE_REMEMBER_ME_LOGIN);

                if (StringUtils.isNotEmpty(encodedLogin)) {
                    String login = URLEncodeUtils.decodeUtf8(encodedLogin);
                    String rememberMeToken = getApp().getCookieValue(LoginCookies.COOKIE_REMEMBER_ME_PASSWORD);

                    if (StringUtils.isNotEmpty(rememberMeToken)) {
                        result = login.equals(rememberMeCredentials.getLogin())
                                && rememberMeToken.equals(rememberMeCredentials.getPassword());
                    }
                }

            }
        }

        return result;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 10;
    }
}