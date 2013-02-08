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
// TODO: A lot of the intents stuff can be extracted to a more generic file that can be used by other query widgets
var intentsLauncher = new neon.eventing.OWFIntentsLauncher();
var dataSourceName = 'charitynetd';
var dataSetId = 'transactions';

$(document).ready(function () {
    OWF.ready(function () {

        var currentCharityFilterId;
        var owfEventPublisher;

        addIntentsMetadata();
        initOWFEventPublisher();
        populateCharitiesList();

        function populateCharitiesList() {
            with (neon.query) {
                var query = new Query().selectFrom(dataSourceName, dataSetId).distinct('charity_name', ASC).includeFiltered(true);
                executeQuery(query, function (result) {
                    addCharitiesToDropdown(result.data);
                    addCharitiesChangeListener();
                });
            }
        }

        function addCharitiesToDropdown(data) {
            var charities = $('select[name="charities"]');
            charities.append($("<option />"));
            data.forEach(function (charity) {
                charities.append($("<option />").val(charity).text(charity));
            });
        }

        function addCharitiesChangeListener() {
            $('select[name="charities"]').change(function (evt) {
                var charityFilter = createCharityFilter();
                if (currentCharityFilterId) {
                    owfEventPublisher.replaceFilter(currentCharityFilterId, charityFilter)
                }
                else {
                    owfEventPublisher.addFilter(charityFilter);
                }
            });
        }

        function initOWFEventPublisher() {
            var messageHandler = new neon.eventing.MessageHandler({
                filtersChanged: function (message) {
                    if (message._source === messageHandler.id) {
                        currentCharityFilterId = message.addedIds[0];
                    }
                }
            });
            owfEventPublisher = new neon.query.OWFEventPublisher(messageHandler);
        }

        function createCharityFilter() {
            var selectedCharity = $('select[name="charities"]').val();
            return new neon.query.Filter().selectFrom(dataSourceName, dataSetId).where('charity_name', '=', selectedCharity);
        }

        function addIntentsMetadata() {
            addMapMetadata();
        }

        function addMapMetadata() {
            var metadata = {};
            metadata.categories = ['donor_state'];
            metadata.aggregateField = 'amount';
            metadata.aggregationOperation = neon.query.SUM;
            intentsLauncher.addMetadataForDataType('application/vnd.neon.map.categorical', metadata);
        }
    });
});