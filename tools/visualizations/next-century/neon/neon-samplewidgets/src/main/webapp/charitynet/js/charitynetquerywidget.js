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
var dataSourceName = 'CharityNet';
var transactionsDatasetId = 'CharityNet_Donations';
var charitiesDatasetId = 'CharityNet_Donations';

$(document).ready(function () {
    OWF.ready(function () {

        var currentCharityFilterId;
        var owfEventPublisher;
        var charitySelected = false;

        addIntentsMetadata();
        initOWFEventPublisher();
        populateCharitiesList();

        function populateCharitiesList() {
            with (neon.query) {
                var query = new Query().selectFrom(dataSourceName, charitiesDatasetId).sortBy('charity_name', ASCENDING);
                executeQuery(query, function (result) {
                    addCharitiesToDropdown(result.data);
                    addCharitiesChangeListener();
                    addDonationsChangeListener();
                });
            }
        }

        function addCharitiesToDropdown(data) {
            var charities = $('select[name="charities"]');
            charities.append($("<option />"));
            data.forEach(function (row) {
                var charity = row.charity_name;
                charities.append($("<option />").val(charity).text(charity));
            });
        }

        function addCharitiesChangeListener() {
            $('select[name="charities"]').change(function (evt) {
                charitySelected = true;
                updateFilter();
            });
        }

        function addDonationsChangeListener() {
            $('input[name="donations"]').click(function (evt) {
                updateFilter()
            });
        }

        function updateFilter() {
            // the charitySelected flag ensures that a change to the donations view doesn't update the filters until
            // a charity has been selected
            if ( charitySelected ) {
                var charityFilter = createCharityFilter();
                if (currentCharityFilterId) {
                    owfEventPublisher.replaceFilter(currentCharityFilterId, charityFilter)
                }
                else {
                    owfEventPublisher.addFilter(charityFilter);
                }
            }
        }


        function initOWFEventPublisher() {
            var messageHandler = new neon.eventing.MessageHandler({
                filtersChanged: function (message) {
                    if (message._source === messageHandler.id) {
                        currentCharityFilterId = message.addedIds[0];
                    }
                }
            });
            owfEventPublisher = new neon.eventing.OWFEventPublisher(messageHandler);
        }

        function createCharityFilter() {
            var selectedCharity = $('select[name="charities"]').val();
            var charityOption = parseInt($('input[name="donations"]:checked').val()
            );
            var filter = new neon.query.Filter().selectFrom(dataSourceName, transactionsDatasetId).where('charity_name', '=', selectedCharity);
            if ( charityOption === 0 ) {
                return filter;
            }
            else if ( charityOption === 1 ) {
                return new neon.query.SubfilterFieldProvider(filter,'donor_accountNumber','in');
            }
            else if ( charityOption === 2 ) {
                return new neon.query.SubfilterFieldProvider(filter,'donor_accountNumber','notin');
            }
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