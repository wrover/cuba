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

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.ClientType;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.security.auth.AbstractClientCredentials;
import com.haulmont.cuba.security.auth.AuthenticationDetails;
import com.haulmont.cuba.security.auth.AuthenticationService;
import com.haulmont.cuba.security.auth.Credentials;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.SessionParams;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.Connection;
import com.haulmont.cuba.web.auth.credentials.LoginCredentials;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;

import javax.inject.Inject;
import java.util.Map;

/**
 * {@link LoginProvider} that implements the "Chain of Responsibility" pattern.
 * It checks if the user is already authenticated or not.
 * If the user is not yet authenticated it checks if it can authenticate him.
 * Regardless of the outcome it passes authorization details to a next LoginProvider.
 *
 * Provider can implement {@link #afterAll(boolean, LoginCredentials)} to put there some logic that
 *  has to be called after all providers had their chance to analyze authentication info.
 *
 * Defining and initializing the next provider is a responsibility of a system that uses
 *  login providers.
 */
abstract public class AbstractLoginProvider implements LoginProvider {

    @Inject
    protected AuthenticationService authenticationService;
    @Inject
    protected GlobalConfig globalConfig;

    protected LoginProvider nextLoginProvider;

    @Override
    public final AuthenticationStatus process(AuthenticationStatus status, LoginCredentials credentials) throws LoginException {

        AuthenticationStatus result = status;

        before(result.isSuccess(), credentials);

        if (!result.isSuccess()) {
            result = tryToAuthenticate(credentials);
        }

        after(result.isSuccess(), credentials);

        if (nextLoginProvider != null) {
            result = nextLoginProvider.process(result, credentials);
        }

        afterAll(result.isSuccess(), credentials);

        return result;
    }

    public void setNextLoginProvider(LoginProvider nextLoginProvider) {
        this.nextLoginProvider = nextLoginProvider;
    }

    /**
     * Authenticate the user if possible.
     *
     * @param credentials          input provided by the user
     * @return                  whether the method is succeeded to authorize the user
     * @throws LoginException   if the input provided by the user is incorrect
     */
    abstract protected AuthenticationStatus tryToAuthenticate(LoginCredentials credentials) throws LoginException;

    protected App getApp() {
        return App.getInstance();
    }

    protected Connection getConnection() {
        return getApp().getConnection();
    }

    protected void before(boolean authenticated, LoginCredentials credentials) {}

    protected void after(boolean authenticated, LoginCredentials credentials) {}

    /**
     * This hook can be used to place there some logic that should be executed
     *  no matter if the user is authorized or not.
     * This method is guaranteed to be called after all Login Providers had a chance to authorize a user.
     */
    protected void afterAll(boolean authenticated, LoginCredentials credentials) {}

    protected UserSession login(Credentials credentials) throws LoginException {

        if (!(credentials instanceof AbstractClientCredentials)) {
            throw new IllegalArgumentException(String.format("Credentials of class %s are not supported", credentials.getClass()));
        }

        AbstractClientCredentials clientCredentials = (AbstractClientCredentials) credentials;

        if (clientCredentials.getLocale() == null) {
            throw new IllegalArgumentException("Locale is null");
        }

        setCredentialsParams(clientCredentials, getLoginParams());

        AuthenticationDetails details = authenticationService.login(credentials);
        return details.getSession();
    }

    protected void setCredentialsParams(AbstractClientCredentials credentials, Map<String, Object> loginParams) {
        credentials.setClientInfo(makeClientInfo());
        credentials.setClientType(ClientType.WEB);
        credentials.setIpAddress(App.getInstance().getClientAddress());
        credentials.setParams(loginParams);
        if (!globalConfig.getLocaleSelectVisible()) {
            credentials.setOverrideLocale(false);
        }
    }

    protected Map<String, Object> getLoginParams() {
        return ParamsMap.of(
                ClientType.class.getName(), ClientType.WEB.name(),
                SessionParams.IP_ADDERSS.getId(), App.getInstance().getClientAddress(),
                SessionParams.CLIENT_INFO.getId(), makeClientInfo()
        );
    }

    protected String makeClientInfo() {
        // timezone info is passed only on VaadinSession creation
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        webBrowser.updateRequestDetails(VaadinService.getCurrentRequest());

        //noinspection UnnecessaryLocalVariable
        String serverInfo = String.format("Web (%s:%s/%s) %s",
                globalConfig.getWebHostName(),
                globalConfig.getWebPort(),
                globalConfig.getWebContextName(),
                webBrowser.getBrowserApplication());

        return serverInfo;
    }

}
