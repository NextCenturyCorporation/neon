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
 */

/**
 * Stubs out OWF if it does not exist, and provides neon.ready()
 * which widgets should use before doing any OWF related work.
 * @class neon
 * @static
 */

if (typeof (OWF) === "undefined" || !OWF.Util.isRunningInOWF()) {
    window.OWF = {
        getIframeId: function () {
            return null;
        },
        getInstanceId: function () {
            return null;
        },
        ready: function(fnToExecute){
            fnToExecute();
        },
        Eventing: {
            publish: function () {
            },
            subscribe: function () {
            }
        }
    };
}

/**
 * Runs a function after the dom is loaded
 * @param functionToRun  the function to run.
 * @method ready
 */

neon.ready = function(functionToRun){
  $(function(){
     OWF.ready(functionToRun);
  });
};