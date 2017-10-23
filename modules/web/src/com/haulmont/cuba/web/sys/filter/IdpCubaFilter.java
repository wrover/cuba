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

package com.haulmont.cuba.web.sys.filter;

import com.google.common.base.Strings;
import com.haulmont.bali.util.URLEncodeUtils;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.security.global.IdpSession;
import com.haulmont.cuba.web.auth.IdpAuthManager;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Checks if the application uses IDP.
 * If so, then the session is checked and the user is redirected to an identity provider if required.
 */
@Component("cuba_IdpCubaFilter")
public class IdpCubaFilter implements CubaFilter, Ordered {

    private static Logger log = LoggerFactory.getLogger(IdpCubaFilter.class);

    protected List<String> bypassUrls = new ArrayList<>();

    protected Lock sessionCheckLock = new ReentrantLock();

    @Inject
    protected Configuration configuration;
    @Inject
    protected IdpAuthManager idpAuthManager;

    @PostConstruct
    public void init() {
        if (isIdpEnabled()) {
            String urls = configuration.getConfig(WebAuthConfig.class).getIdpBypassUrls();
            String[] strings = urls.split("[, ]");
            for (String string : strings) {
                if (StringUtils.isNotBlank(string)) {
                    bypassUrls.add(string);
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        boolean filtered = false;

        if (isIdpEnabled()) {

            String requestURI = ((HttpServletRequest) request).getRequestURI();

            if (!requestURI.endsWith("/")) {
                requestURI = requestURI + "/";
            }

            boolean bypass = false;
            for (String bypassUrl : bypassUrls) {
                if (requestURI.contains(bypassUrl)) {
                    log.debug("Skip idp auth for by pass url: " + bypassUrl);
                    bypass = true;
                    break;
                }
            }

            if (!bypass) {
                assertIdpSession(request, response, chain);
                filtered = true;
            }
        }

        if (!filtered) {
            chain.doFilter(request, response);
        }
    }

    @Override
    public int getOrder() {
        return 110;
    }

    protected void assertIdpSession(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // send static files without authentication
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (StringUtils.startsWith(httpRequest.getRequestURI(), httpRequest.getContextPath() + "/VAADIN/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String idpBaseURL = configuration.getConfig(WebAuthConfig.class).getIdpBaseURL();
        if (Strings.isNullOrEmpty(idpBaseURL)) {
            log.error("Application property cuba.web.idp.url is not set");
            httpResponse.setStatus(500);
            return;
        }

        if (!idpBaseURL.endsWith("/")) {
            idpBaseURL += "/";
        }

        String requestUrl = httpRequest.getRequestURL().toString();
        if (StringUtils.startsWith(requestUrl, idpBaseURL)) {
            chain.doFilter(httpRequest, response);
            return;
        }

        HttpSession session = httpRequest.getSession(true);
        Lock sessionLock = (Lock) session.getAttribute(IdpAuthManager.IDP_SESSION_LOCK_ATTRIBUTE);
        if (sessionLock == null) {
            sessionCheckLock.lock();
            try {
                sessionLock = (Lock) session.getAttribute(IdpAuthManager.IDP_SESSION_LOCK_ATTRIBUTE);
                if (sessionLock == null) {
                    sessionLock = new ReentrantLock();
                    session.setAttribute(IdpAuthManager.IDP_SESSION_LOCK_ATTRIBUTE, sessionLock);
                }
            } finally {
                sessionCheckLock.unlock();
            }
        }

        IdpSession boundIdpSession;
        sessionLock.lock();

        try {
            session.getAttribute(IdpAuthManager.IDP_SESSION_LOCK_ATTRIBUTE);
        } catch (IllegalStateException e) {
            // Someone might have invalidated the session between fetching the lock and acquiring it.
            sessionLock.unlock();

            log.debug("Invalidated session {}", session.getId());
            httpResponse.sendRedirect(httpRequest.getRequestURL().toString());
            return;
        }

        try {
            if ("GET".equals(httpRequest.getMethod())
                    && httpRequest.getParameter(IdpAuthManager.IDP_TICKET_REQUEST_PARAM) != null) {
                String idpTicket = httpRequest.getParameter(IdpAuthManager.IDP_TICKET_REQUEST_PARAM);

                IdpSession idpSession;
                try {
                    idpSession = idpAuthManager.getIdpSession(idpTicket);
                } catch (IdpAuthManager.IdpActivationException e) {
                    log.error("Unable to obtain IDP session by ticket", e);
                    httpResponse.setStatus(500);
                    return;
                }

                if (idpSession == null) {
                    log.warn("Used old IDP ticket {}, send redirect", idpTicket);
                    // used old ticket, send redirect
                    httpResponse.sendRedirect(getIdpRedirectUrl());
                    return;
                }

                session.invalidate();

                session = httpRequest.getSession(true);
                session.setAttribute(IdpAuthManager.IDP_SESSION_LOCK_ATTRIBUTE, sessionLock);
                session.setAttribute(IdpAuthManager.IDP_SESSION_ATTRIBUTE, idpSession);

                log.debug("IDP session {} obtained, redirect to application", idpSession);

                // redirect to application without parameters
                httpResponse.sendRedirect(httpRequest.getRequestURL().toString());
                return;
            }

            if (session.getAttribute(IdpAuthManager.IDP_SESSION_ATTRIBUTE) == null) {
                if ("GET".equals(httpRequest.getMethod())
                        && !StringUtils.startsWith(httpRequest.getRequestURI(), httpRequest.getContextPath() + "/PUSH")) {
                    httpResponse.sendRedirect(getIdpRedirectUrl());
                }
                return;
            }

            boundIdpSession = (IdpSession) session.getAttribute(IdpAuthManager.IDP_SESSION_ATTRIBUTE);
        } finally {
            sessionLock.unlock();
        }

        HttpServletRequest authenticatedRequest = new IdpServletRequestWrapper(httpRequest,
                new IdpAuthManager.IdpSessionPrincipal(boundIdpSession));

        chain.doFilter(authenticatedRequest, response);
    }

    protected String getIdpRedirectUrl() {
        String idpBaseURL = configuration.getConfig(WebAuthConfig.class).getIdpBaseURL();
        if (!idpBaseURL.endsWith("/")) {
            idpBaseURL += "/";
        }

        return idpBaseURL + "?sp=" +
                URLEncodeUtils.encodeUtf8(configuration.getConfig(GlobalConfig.class).getWebAppUrl());
    }

    protected boolean isIdpEnabled() {
        return configuration.getConfig(WebAuthConfig.class).getUseIdpAuthentication();
    }

    static class IdpServletRequestWrapper extends HttpServletRequestWrapper {

        private final IdpAuthManager.IdpSessionPrincipal principal;

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request The request to wrap
         * @throws IllegalArgumentException if the request is null
         */
        public IdpServletRequestWrapper(HttpServletRequest request, IdpAuthManager.IdpSessionPrincipal principal) {
            super(request);
            this.principal = principal;
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }

        @Override
        public Locale getLocale() {
            if (principal.getLocale() != null) {
                return principal.getLocale();
            }

            return super.getLocale();
        }
    }

}
