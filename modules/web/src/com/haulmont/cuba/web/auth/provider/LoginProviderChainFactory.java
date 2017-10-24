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
import com.haulmont.cuba.web.auth.credentials.LoginCredentials;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component(LoginProviderChainFactory.NAME)
public class LoginProviderChainFactory {

    public static final String NAME = "cuba_LoginProviderChainFactory";

    @Inject
    protected List<LoginProvider> loginProviders;

    public LoginProviderChain create() {
        return new DefaultLoginProviderChain(this.loginProviders);
    }

    protected static class DefaultLoginProviderChain implements LoginProviderChain {

        protected int currentPosition = 0;
        protected List<LoginProvider> loginProviders;

        protected DefaultLoginProviderChain(List<LoginProvider> loginProviders) {
            this.loginProviders = loginProviders;
        }

        @Override
        public AuthenticationStatus process(AuthenticationStatus status, LoginCredentials credentials) throws LoginException {
            return nextProvider(status, credentials);
        }

        @Override
        public AuthenticationStatus nextProvider(AuthenticationStatus status, LoginCredentials credentials) throws LoginException {
            if (currentPosition < loginProviders.size()) {
                LoginProvider provider = loginProviders.get(currentPosition);
                ++ currentPosition;
                return provider.process(status, credentials, this);
            } else {
                return status;
            }
        }

    }

}
