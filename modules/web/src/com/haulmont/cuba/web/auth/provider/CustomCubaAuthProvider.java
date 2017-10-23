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
import com.haulmont.cuba.web.auth.CubaAuthProvider;
import com.haulmont.cuba.web.auth.DomainAliasesResolver;
import com.haulmont.cuba.web.auth.ExternallyAuthenticatedConnection;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import com.haulmont.cuba.web.auth.credentials.LdapCredentials;
import com.haulmont.cuba.web.auth.credentials.LoginCredentials;
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
    @Inject
    protected DomainAliasesResolver domainAliasesResolver;

    @Override
    protected boolean tryToAuthenticate(LoginCredentials credentials) throws LoginException {

        boolean result = false;

        if (credentials instanceof LdapCredentials) {
            LdapCredentials ldapCredentials = (LdapCredentials) credentials;

            cubaAuthProvider.authenticate(ldapCredentials.getLogin(), ldapCredentials.getPassword(), ldapCredentials.getLocale());
            ((ExternallyAuthenticatedConnection) getConnection()).loginAfterExternalAuthentication(
                    convertLoginString(ldapCredentials.getLogin()), ldapCredentials.getLocale()
            );
            result = true;
        }

        return result;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 25;
    }

    /**
     * Convert userName to db form
     * In database users stores in form DOMAIN&#92;userName
     *
     * @param login Login string
     * @return login in form DOMAIN&#92;userName
     */
    protected String convertLoginString(String login) {
        int slashPos = login.indexOf("\\");
        if (slashPos >= 0) {
            String domainAlias = login.substring(0, slashPos);
            String domain = domainAliasesResolver.getDomainName(domainAlias).toUpperCase();
            String userName = login.substring(slashPos + 1);
            login = domain + "\\" + userName;
        } else {
            int atSignPos = login.indexOf("@");
            if (atSignPos >= 0) {
                String domainAlias = login.substring(atSignPos + 1);
                String domain = domainAliasesResolver.getDomainName(domainAlias).toUpperCase();
                String userName = login.substring(0, atSignPos);
                login = domain + "\\" + userName;
            }
        }
        return login;
    }
}
