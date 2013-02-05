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
var dataSourceName;
var datasetId;
var connected = false;

$(document).ready(function () {
    OWF.ready(function () {
        var table = new neon.table.Table();

        // right now the message handler only receives messages (which happens just by creating it),
        // but in the future we might want to send messages based on actions performed on the table
        var messageHandler = new neon.eventing.MessageHandler({
            selectionChanged: updateData,
            filtersChanged: updateData
        });

        OWF.Intents.receive(
            {
                action: 'table',
                dataType: 'application/vnd.neon.tabular'
            },
            function (sender, intent, data) {
                connected = true;
                dataSourceName = data.dataSourceName;
                datasetId = data.datasetId;
                updateData();
            }
        );

        table.show();
        $('#initializing').remove();


        function updateData() {
            if (connected) {
                with (neon.query) {
                    // TODO: There should be a way to get all selected items and an easy way to just check if items are selected
                    getSelectionWhere(new Filter().selectFrom(dataSourceName, datasetId), function (selected) {
                        if (selected.data.length > 0) {
                            table.setData(selected.data);
                        }
                        else {
                            executeQuery(new Query().selectFrom(dataSourceName, datasetId), function (all) {
                                table.setData(all.data);
                            });
                        }
                    });
                }
            }
        }

    });
});

