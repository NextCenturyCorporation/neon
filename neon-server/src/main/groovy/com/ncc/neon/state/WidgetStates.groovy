package com.ncc.neon.state

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext

/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 * @author tbrooks
 */

/**
 * A cache for all the widget states for a given user.
 */

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class WidgetStates implements Serializable{

    private static final long serialVersionUID = - 5362959301029628371L

    // the states are transient because we don't actually want to save them between user sessions
    private transient Set<WidgetState> states

    WidgetStates() {
        initEmptyStates()
    }

    private void initEmptyStates() {
        states = [] as Set
    }

    /**
     * Creates a new widget state for the current user.
     * @param clientId An identifier specified by the client. This is typically the widget name, e,g, "Map"
     * @param json data that is stored that a widget can use to set it's state.
     */

    void addWidgetState(String clientId, String json) {
        if(!clientId){
            return
        }

        WidgetState widgetState = new WidgetState(clientId, json)
        states.remove(widgetState)
        states.add(widgetState)
    }

    /**
     * Gets the widget state from the session
     * @param clientId The clientId must match a previous addition
     * @return the WidgetState or null if none is found.
     */
    WidgetState getWidgetState(String clientId) {
        states.find {
            clientId == it.clientId
        }
    }

    @SuppressWarnings("UnusedPrivateMethod") // needed for deserialization
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject()
        initEmptyStates()
    }


}
