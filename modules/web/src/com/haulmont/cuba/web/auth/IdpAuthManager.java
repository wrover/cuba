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

package com.haulmont.cuba.web.auth;

import com.haulmont.cuba.security.global.IdpSession;

import javax.annotation.Nullable;
import java.security.Principal;
import java.util.Locale;

public interface IdpAuthManager {

    String NAME = "cuba_IdpAuthManager";

    String IDP_SESSION_ATTRIBUTE = "IDP_SESSION";
    String IDP_SESSION_LOCK_ATTRIBUTE = "IDP_SESSION_LOCK";
    String IDP_TICKET_REQUEST_PARAM = "idp_ticket";

    IdpSession getIdpSession(String idpTicket) throws IdpActivationException;

    class IdpActivationException extends Exception {
        public IdpActivationException(String message) {
            super(message);
        }

        public IdpActivationException(Throwable cause) {
            super(cause);
        }

        public IdpActivationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    class IdpSessionPrincipal implements Principal {
        private final IdpSession idpSession;

        public IdpSessionPrincipal(IdpSession idpSession) {
            this.idpSession = idpSession;
        }

        @Override
        public String getName() {
            return idpSession.getLogin();
        }

        public IdpSession getIdpSession() {
            return idpSession;
        }

        @Nullable
        public Locale getLocale() {
            String locale = idpSession.getLocale();
            if (locale == null) {
                return null;
            }

            return Locale.forLanguageTag(locale);
        }
    }

}