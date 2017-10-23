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

package com.haulmont.cuba.web.auth;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.haulmont.bali.util.URLEncodeUtils;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.auth.AuthenticationService;
import com.haulmont.cuba.security.global.IdpSession;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.NoUserSessionException;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.security.idp.IdpService;
import com.haulmont.cuba.web.auth.credentials.TrustedCredentials;
import com.haulmont.cuba.web.event.ExternalAuthenticationInitEvent;
import com.haulmont.cuba.web.event.LogoutEvent;
import com.haulmont.cuba.web.event.PingSessionEvent;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;

/**
 * {@link IdpAuthManager} that best suites to situation when Service Provider is also a CUBA application.
 */
@Component(IdpAuthManager.NAME)
public class DefaultIdpAuthManagerBean implements IdpAuthManager {

    private final Logger log = LoggerFactory.getLogger(DefaultIdpAuthManagerBean.class);

    @Inject
    protected WebAuthConfig webAuthConfig;

    @Inject
    protected GlobalConfig globalConfig;

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected AuthenticationService authenticationService;

    public IdpSession getIdpSession(String idpTicket) throws IdpActivationException {
        String idpBaseURL = webAuthConfig.getIdpBaseURL();
        if (!idpBaseURL.endsWith("/")) {
            idpBaseURL += "/";
        }
        String idpTicketActivateUrl = idpBaseURL + "service/activate";

        HttpPost httpPost = new HttpPost(idpTicketActivateUrl);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());

        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(Arrays.asList(
                new BasicNameValuePair("serviceProviderTicket", idpTicket),
                new BasicNameValuePair("trustedServicePassword", webAuthConfig.getIdpTrustedServicePassword())
        ), StandardCharsets.UTF_8);

        httpPost.setEntity(formEntity);

        HttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
        HttpClient client = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .build();

        String idpResponse;
        try {
            HttpResponse httpResponse = client.execute(httpPost);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 410) {
                // used old ticket
                return null;
            }

            if (statusCode != 200) {
                throw new IdpActivationException("Idp respond with status " + statusCode);
            }

            idpResponse = new BasicResponseHandler().handleResponse(httpResponse);
        } catch (IOException e) {
            throw new IdpActivationException(e);
        } finally {
            connectionManager.shutdown();
        }

        IdpSession session;
        try {
            session = new Gson().fromJson(idpResponse, IdpSession.class);
        } catch (JsonSyntaxException e) {
            throw new IdpActivationException("Unable to parse idp response", e);
        }

        return session;
    }

    @EventListener
    public void onPingEvent(PingSessionEvent event) {
        if (webAuthConfig.getUseIdpAuthentication()) {
            UserSession session = event.getSession();

            if (session != null) {
                String idpSessionId = event.getSession().getAttribute(IdpService.IDP_USER_SESSION_ATTRIBUTE);
                if (idpSessionId != null) {
                    pingIdpSessionServer(idpSessionId);
                }
            }
        }
    }

    @EventListener
    public void onExternalAuthenticationInitEvent(ExternalAuthenticationInitEvent event) {
        if (webAuthConfig.getUseIdpAuthentication() && !event.isAuthenticated()) {
            try {
                event.getConnection().login(
                        new TrustedCredentials(event.getUserName(), event.getLocale())
                );
            } catch (LoginException e) {
                throw new RuntimeException("Error on trusted client login", e);
            }

            UserSession session = event.getConnection().getSession();

            RequestContext requestContext = RequestContext.get();

            if (requestContext != null) {
                Principal principal = requestContext.getRequest().getUserPrincipal();

                if (principal instanceof IdpSessionPrincipal) {
                    IdpSession idpSession = ((IdpSessionPrincipal) principal).getIdpSession();
                    session.setAttribute(IdpService.IDP_USER_SESSION_ATTRIBUTE, idpSession.getId());
                }
            }

            event.setAuthenticated(true);
        }
    }

    @EventListener
    public void onLogoutEvent(LogoutEvent event) {
        RequestContext requestContext = RequestContext.get();
        if (requestContext != null) {
            requestContext.getSession().removeAttribute(IDP_SESSION_ATTRIBUTE);
        }

        String idpBaseURL = webAuthConfig.getIdpBaseURL();
        if (Strings.isNullOrEmpty(idpBaseURL)) {
            log.error("Application property cuba.web.idp.url is not set");
        } else {
            event.setLoggedOutUrl(getIdpLogoutUrl());
        }
    }

    protected void pingIdpSessionServer(String idpSessionId) {
        log.debug("Ping IDP session {}", idpSessionId);

        String idpBaseURL = webAuthConfig.getIdpBaseURL();
        if (!idpBaseURL.endsWith("/")) {
            idpBaseURL += "/";
        }
        String idpSessionPingUrl = idpBaseURL + "service/ping";

        HttpPost httpPost = new HttpPost(idpSessionPingUrl);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());

        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(Arrays.asList(
                new BasicNameValuePair("idpSessionId", idpSessionId),
                new BasicNameValuePair("trustedServicePassword", webAuthConfig.getIdpTrustedServicePassword())
        ), StandardCharsets.UTF_8);

        httpPost.setEntity(formEntity);

        HttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
        HttpClient client = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .build();

        try {
            HttpResponse httpResponse = client.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 410) {
                // we have to logout user
                log.debug("IDP session is expired {}", idpSessionId);

                if (userSessionSource.checkCurrentUserSession()) {
                    authenticationService.logout();

                    UserSession userSession = userSessionSource.getUserSession();

                    throw new NoUserSessionException(userSession.getId());
                }
            }
            if (statusCode != 200) {
                log.warn("IDP respond status {} on session ping", statusCode);
            }
        } catch (IOException e) {
            log.warn("Unable to ping IDP {} session {}", idpSessionPingUrl, idpSessionId, e);
        } finally {
            connectionManager.shutdown();
        }
    }

    protected String getIdpLogoutUrl() {
        String idpBaseURL = webAuthConfig.getIdpBaseURL();
        if (!idpBaseURL.endsWith("/")) {
            idpBaseURL += "/";
        }

        return idpBaseURL + "logout?sp=" +
                URLEncodeUtils.encodeUtf8(globalConfig.getWebAppUrl());
    }
}