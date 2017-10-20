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


import com.haulmont.cuba.web.auth.CubaAuthProvider;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.*;
import java.io.IOException;

/**
 * @deprecated
 * Provides back-compatibility.
 * Injects custom {@link CubaAuthProvider} into the common filter chain.
 * Scheduled to be removed in CUBA 7.0
 */
@Component("cuba_LegacyAuthCubaFilter")
@Deprecated
public class LegacyAuthCubaFilter implements CubaFilter, Ordered {

    @Inject
    protected CubaAuthProvider cubaAuthProvider;
    @Inject
    protected WebAuthConfig webAuthConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (webAuthConfig.getExternalAuthentication()) {
            cubaAuthProvider.init(filterConfig);
        }
    }

    @Override
    public void destroy() {
        if (webAuthConfig.getExternalAuthentication()) {
            cubaAuthProvider.destroy();
        }

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // If external authentication == false, there still can be a custom CubaAuthProvider.
        // And this provider can have empty doFilter() method that doesn't call chain.doFilter().
        // But if externalAuthentication is true then the client is certainly implemented doFilter() correctly in his provider.
        if (webAuthConfig.getExternalAuthentication()) {
            cubaAuthProvider.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public int getOrder() {
        return 120;
    }
}
