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

import com.haulmont.cuba.core.global.PasswordEncryption;
import com.haulmont.cuba.security.auth.TrustedClientCredentials;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import com.haulmont.cuba.web.auth.credentials.LoginCredentials;
import com.haulmont.cuba.web.auth.credentials.TrustedCredentials;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("cuba_TrustedLoginProvider")
public class TrustedLoginProvider extends AbstractLoginProvider implements Ordered {

    @Inject
    protected PasswordEncryption passwordEncryption;
    @Inject
    protected WebAuthConfig webAuthConfig;

    @Override
    protected boolean tryToAuthenticate(LoginCredentials credentials) throws LoginException {

        boolean result = false;

        if (credentials instanceof TrustedCredentials) {
            TrustedCredentials trustedCredentials = (TrustedCredentials) credentials;

            getConnection().login(
                new TrustedClientCredentials(
                        trustedCredentials.getUserName(),
                        webAuthConfig.getTrustedClientPassword(),
                        trustedCredentials.getLocale()
                )
            );

            result = true;
        }

        return result;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 10;
    }
}