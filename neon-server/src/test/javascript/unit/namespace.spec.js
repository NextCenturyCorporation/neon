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
describe('namespace', function() {

    it('create a non existing namespace', function() {
        // verify the namespace doesn't already exist
        expect(window['namespacespec-nonexisting']).toBeUndefined();
        neon.namespace('namespacespec-nonexisting.sample.ns');

        expect(window['namespacespec-nonexisting']).toBeDefined();
        expect(window['namespacespec-nonexisting']['sample']).toBeDefined();
        expect(window['namespacespec-nonexisting']['sample']['ns']).toBeDefined();
    });

    it('does not overwrite an existing namespace', function() {
        // verify the namespace doesn't already exist
        expect(window['namespacespec-existing']).toBeUndefined();

        // now create it directly on the window and add a variable to it. it should not be overwritten by the
        // createNamespace function
        window['namespacespec-existing'] = {};
        window['namespacespec-existing']['sample'] = {};
        window['namespacespec-existing']['sample'].myvar = 'hello';
        neon.namespace('namespacespec-existing.sample');
        expect(window['namespacespec-existing']['sample'].myvar).toBe('hello');
    });
});