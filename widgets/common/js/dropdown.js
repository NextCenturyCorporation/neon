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

var neon = neon || {};

neon.dropdown = (function () {

    function populateFieldValues(attributeValues, selectId) {
        var select = $('#' + selectId);
        attributeValues.fieldNames.forEach(function (field) {
            select.append($('<option></option>').attr('value', field).text(field));
        });

        var initialField = attributeValues.metadata.mapping[selectId];
        if(initialField){
            setDropdownInitialValue(selectId, initialField);
            select.change();
        }
    }

    function setDropdownInitialValue (selectElementId, value) {
        $('#' + selectElementId + ' option[value="' + value + '"]').prop('selected', true);
    }

    return {
        setDropdownInitialValue: setDropdownInitialValue,

        getFieldNamesFromDropdown: function (selectElementId) {
            var optionsSelector = $('#' + selectElementId + ' option');
            return {
                fieldNames: $.map(optionsSelector, function (option) {
                    if (!option.value) {
                        return null;
                    }
                    return option.value;
                })
            };
        },

        populateAttributeDropdowns: function (attributeValues, dropDownIds, onChange) {
            var dropDownIdsArray = Array.isArray(dropDownIds) ? dropDownIds : [dropDownIds];
            dropDownIdsArray.forEach(function (selectId) {
                var select = $('#' + selectId);
                select.empty();
                select.append($('<option></option>').attr('value', '').text('(Select Field)'));
                select.change(onChange);
                populateFieldValues(attributeValues, selectId);
            });
        }
    }
})();