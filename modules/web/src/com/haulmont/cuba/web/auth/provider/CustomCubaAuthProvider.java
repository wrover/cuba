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

import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.auth.AuthInfo;
import com.haulmont.cuba.web.auth.CubaAuthProvider;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @deprecated
 * Provides back-compatibility.
 * Injects custom CubaAuthProvider into the chain login providers.
 * Scheduled to be removed in CUBA 7.0
 */
@Deprecated
@Component("cuba_CustomCubaAuthProvider")
public class CustomCubaAuthProvider extends AbstractLoginProvider implements Ordered {

    @Inject
    protected CubaAuthProvider cubaAuthProvider;
    @Inject
    protected WebAuthConfig webAuthConfig;

    @Override
    protected boolean tryToAuthenticate(AuthInfo authInfo) throws LoginException {

        boolean customProviderUsed = webAuthConfig.getExternalAuthentication() && !(
                webAuthConfig.getExternalAuthenticationProviderClass().equalsIgnoreCase("com.haulmont.cuba.web.auth.IdpAuthProvider")
                        || webAuthConfig.getExternalAuthenticationProviderClass().equalsIgnoreCase("com.haulmont.cuba.web.auth.LdapAuthProvider")
        );

        if (customProviderUsed) {
            cubaAuthProvider.authenticate(authInfo.getLogin(), authInfo.getPassword(), authInfo.getLocale());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 15;
    }
}
