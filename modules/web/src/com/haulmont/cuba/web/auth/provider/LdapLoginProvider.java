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

import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.security.auth.TrustedClientCredentials;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.auth.DomainAliasesResolver;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import com.haulmont.cuba.web.auth.credentials.LdapCredentials;
import com.haulmont.cuba.web.auth.credentials.LoginCredentials;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link LoginProvider} that authenticates the user if LDAP integration is enabled for the current application
 *  and the user is not included in the list of people who don't use LDAP authentication.
 */
@Component("cuba_LdapProvider")
public class LdapLoginProvider extends AbstractLoginProvider implements Ordered {

    @Inject
    protected WebAuthConfig webAuthConfig;
    @Inject
    protected DomainAliasesResolver domainAliasesResolver;
    @Inject
    protected Messages messages;

    protected LdapContextSource ldapContextSource;

    protected LdapTemplate ldapTemplate;

    @PostConstruct
    public void init() {

        if (!webAuthConfig.getLdapAuthenticationEnabled()) return;

        ldapContextSource = new LdapContextSource();

        checkRequiredConfigProperties(webAuthConfig);

        ldapContextSource.setBase(webAuthConfig.getLdapBase());
        List<String> ldapUrls = webAuthConfig.getLdapUrls();
        ldapContextSource.setUrls(ldapUrls.toArray(new String[ldapUrls.size()]));
        ldapContextSource.setUserDn(webAuthConfig.getLdapUser());
        ldapContextSource.setPassword(webAuthConfig.getLdapPassword());

        ldapContextSource.afterPropertiesSet();

        ldapTemplate = new LdapTemplate(ldapContextSource);
        ldapTemplate.setIgnorePartialResultException(true);
    }

    @Override
    protected boolean tryToAuthenticate(LoginCredentials credentials) throws LoginException {

        boolean result = false;

        if (credentials instanceof LdapCredentials) {
            LdapCredentials ldapCredentials = (LdapCredentials) credentials;

            authenticateInExternalSystem(ldapCredentials.getLogin(), ldapCredentials.getPassword(), ldapCredentials.getLocale());
            String login = convertLoginString(ldapCredentials.getLogin());

            getConnection().login(
                    new TrustedClientCredentials(login, webAuthConfig.getTrustedClientPassword(), ldapCredentials.getLocale())
            );

            result = true;
        }

        return result;
    }

    /**
     * Converts userName to db form.
     * In database users are stored in the format DOMAIN&#92;userName
     *
     * @param login Login string
     * @return login in the format DOMAIN&#92;userName
     */
    private String convertLoginString(String login) {
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

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 30;
    }

    protected void authenticateInExternalSystem(String login, String password, Locale messagesLocale) throws LoginException {
        if (!ldapTemplate.authenticate(LdapUtils.emptyLdapName(), buildPersonFilter(login), password)) {
            throw new LoginException(
                    messages.formatMessage(LdapLoginProvider.class, "LoginException.InvalidLoginOrPassword", messagesLocale, login)
            );
        }
    }

    protected String buildPersonFilter(String login) {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person"))
                .and(new EqualsFilter(webAuthConfig.getLdapUserLoginField(), login));
        return filter.encode();
    }

    protected void checkRequiredConfigProperties(WebAuthConfig webAuthConfig) {
        List<String> missingProperties = new ArrayList<>();
        if (StringUtils.isBlank(webAuthConfig.getLdapBase())) {
            missingProperties.add("cuba.web.ldap.base");
        }
        if (webAuthConfig.getLdapUrls().isEmpty()) {
            missingProperties.add("cuba.web.ldap.urls");
        }
        if (StringUtils.isBlank(webAuthConfig.getLdapUser())) {
            missingProperties.add("cuba.web.ldap.user");
        }
        if (StringUtils.isBlank(webAuthConfig.getLdapPassword())) {
            missingProperties.add("cuba.web.ldap.password");
        }

        if (!missingProperties.isEmpty()) {
            throw new IllegalStateException("Please configure required application properties for LDAP integration: \n" +
                    StringUtils.join(missingProperties, "\n"));
        }
    }
}