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

import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @deprecated
 * Class for supporting  both {@link WebAuthConfig#getExternalAuthenticationProviderClass()}
 *  and {@link WebAuthConfig#getUseIdpAuthentication()} with {@link WebAuthConfig#getLdapAuthenticationEnabled()}
 * Scheduled to be removed in CUBA 7.0
 */
@Deprecated
@Component("cuba_ExternalAuthenticationSettingsHelper")
public final class ExternalAuthenticationSettingsHelper {

    @Inject
    protected WebAuthConfig webAuthConfig;

    public boolean isLdapUsed() {
        return webAuthConfig.getLdapAuthenticationEnabled() || (webAuthConfig.getExternalAuthentication()
                && webAuthConfig.getExternalAuthenticationProviderClass().equalsIgnoreCase("com.haulmont.cuba.web.auth.LdapAuthProvider"));
    }

    public boolean isIdpUsed() {
        return webAuthConfig.getUseIdpAuthentication() || (webAuthConfig.getExternalAuthentication()
                && webAuthConfig.getExternalAuthenticationProviderClass().equalsIgnoreCase("com.haulmont.cuba.web.auth.IdpAuthProvider"));
    }

    public boolean isIdpOrLdapUsed() {
        return isIdpUsed() || isLdapUsed();
    }
}
