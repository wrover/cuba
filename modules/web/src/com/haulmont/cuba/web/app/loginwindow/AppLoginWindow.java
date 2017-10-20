/*
 * Copyright (c) 2008-2016 Haulmont.
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

package com.haulmont.cuba.web.app.loginwindow;

import com.haulmont.bali.util.URLEncodeUtils;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.WebConfig;
import com.haulmont.cuba.web.auth.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Locale;
import java.util.Map;

public class AppLoginWindow extends AbstractWindow implements Window.TopLevelWindow {

    private static final Logger log = LoggerFactory.getLogger(AppLoginWindow.class);

    protected static final ThreadLocal<AuthInfo> authInfoThreadLocal = new ThreadLocal<>();

    @Inject
    protected GlobalConfig globalConfig;

    @Inject
    protected WebConfig webConfig;

    @Inject
    protected WebAuthConfig webAuthConfig;

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected Embedded logoImage;

    @Inject
    protected TextField loginField;

    @Inject
    protected CheckBox rememberMeCheckBox;

    @Inject
    protected Label rememberMeSpacer;

    @Inject
    protected PasswordField passwordField;

    @Inject
    protected Label localesSelectLabel;

    @Inject
    protected LookupField localesSelect;

    @Inject
    protected  LoginManager loginManager;

    @Deprecated
    protected Boolean bruteForceProtectionEnabled;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        loginField.requestFocus();

        initPoweredByLink();

        initLogoImage();

        initDefaultCredentials();

        initLocales();

        initRememberMe();
    }

    protected void initPoweredByLink() {
        Component poweredByLink = getComponent("poweredByLink");
        if (poweredByLink != null) {
            poweredByLink.setVisible(webConfig.getLoginDialogPoweredByLinkVisible());
        }
    }

    protected void initLocales() {
        Map<String, Locale> locales = globalConfig.getAvailableLocales();

        localesSelect.setOptionsMap(locales);
        localesSelect.setValue(App.getInstance().getLocale());

        boolean localeSelectVisible = globalConfig.getLocaleSelectVisible();
        localesSelect.setVisible(localeSelectVisible);
        localesSelectLabel.setVisible(localeSelectVisible);

        localesSelect.addValueChangeListener(e -> {
            Locale selectedLocale = (Locale) e.getValue();

            App app = App.getInstance();
            app.setLocale(selectedLocale);

            authInfoThreadLocal.set(getAuthInfo());
            try {
                app.createTopLevelWindow();
            } finally {
                authInfoThreadLocal.set(null);
            }
        });
    }

    protected void initLogoImage() {
        String loginLogoImagePath = messages.getMainMessage("loginWindow.logoImage", userSessionSource.getLocale());
        if (StringUtils.isBlank(loginLogoImagePath) || "loginWindow.logoImage".equals(loginLogoImagePath)) {
            logoImage.setVisible(false);
        } else {
            logoImage.setSource("theme://" + loginLogoImagePath);
        }
    }

    protected void initRememberMe() {
        if (!webConfig.getRememberMeEnabled()) {
            rememberMeCheckBox.setValue(false);

            rememberMeSpacer.setVisible(false);
            rememberMeCheckBox.setVisible(false);
            return;
        }

        App app = App.getInstance();

        String rememberMeCookie = app.getCookieValue(LoginCookies.COOKIE_REMEMBER_ME_USED);
        if (Boolean.parseBoolean(rememberMeCookie)) {
            String encodedLogin = app.getCookieValue(LoginCookies.COOKIE_REMEMBER_ME_LOGIN);
            String rememberMeToken = app.getCookieValue(LoginCookies.COOKIE_REMEMBER_ME_PASSWORD);

            if (StringUtils.isNotEmpty(rememberMeToken)) {

                String login = encodedLogin == null ? "" : URLEncodeUtils.decodeUtf8(encodedLogin);

                rememberMeCheckBox.setValue(true);
                loginField.setValue(login);

                passwordField.setValue(rememberMeToken);
            }
        }
    }

    protected void initDefaultCredentials() {
        AuthInfo authInfo = authInfoThreadLocal.get();
        if (authInfo != null) {
            loginField.setValue(authInfo.getLogin());
            passwordField.setValue(authInfo.getPassword());
            rememberMeCheckBox.setValue(authInfo.getRememberMe());

            localesSelect.requestFocus();
        } else {
            if (webAuthConfig.getLdapAuthenticationEnabled()) {

                App app = App.getInstance();

                loginField.setValue(app.getPrincipal() == null ? "" : app.getPrincipal().getName());
                passwordField.setValue("");
            } else {
                String defaultUser = webConfig.getLoginDialogDefaultUser();
                if (!StringUtils.isBlank(defaultUser) && !"<disabled>".equals(defaultUser)) {
                    loginField.setValue(defaultUser);
                } else {
                    loginField.setValue("");
                }

                String defaultPassw = webConfig.getLoginDialogDefaultPassword();
                if (!StringUtils.isBlank(defaultPassw) && !"<disabled>".equals(defaultPassw)) {
                    passwordField.setValue(defaultPassw);
                } else {
                    passwordField.setValue("");
                }
            }
        }
    }

    protected void showUnhandledExceptionOnLogin(@SuppressWarnings("unused") Exception e) {
        showLoginException(messages.getMainMessage("loginWindow.pleaseContactAdministrator", userSessionSource.getLocale()));
    }

    protected void showLoginException(String message) {
        String title = messages.getMainMessage("loginWindow.loginFailed", userSessionSource.getLocale());

        showNotification(title, message, NotificationType.ERROR);
    }

    public void login() {
        try {
            loginManager.login(getAuthInfo());
        } catch (LoginException e) {
            log.info("Login failed: {}", e.toString());

            String message = StringUtils.abbreviate(e.getMessage(), 1000);
            showLoginException(message);
        } catch (Exception e) {
            log.warn("Unable to login", e);

            showUnhandledExceptionOnLogin(e);
        }
    }

    @Deprecated
    protected boolean isBruteForceProtectionEnabled() {
        return false;
    }

    @Deprecated
    protected boolean bruteForceProtectionCheck(String login, String ipAddress) {
        return true;
    }

    @Deprecated
    @Nullable
    protected String registerUnsuccessfulLoginAttempt(String login, String ipAddress) {
        return null;
    }

    /**
     * Override this method if you extended the login screen and added more input elements
     */
    protected AuthInfo getAuthInfo() {
        return new AuthInfo(
                loginField.getValue(),
                passwordField.getValue(),
                rememberMeCheckBox.getValue(),
                localesSelect.getValue()
        );
    }

}