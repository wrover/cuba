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
 *
 */
package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.web.sys.filter.CubaFilter;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This filter just represents a group of CUBA filters
 */
public class CubaHttpFilter extends CompositeFilter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        List<Filter> cubaFilters = new ArrayList<>(AppBeans.getAll(CubaFilter.class).values());

        setFilters(cubaFilters);

        super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        super.doFilter(request, response, chain);

    }

}