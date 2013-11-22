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

    function populateFieldValues(attributeValues, element) {
        var select = $('#' + element.id);

        attributeValues.data.forEach(function (field) {
            if(!element.metadata || !attributeValues.metadata || $.isEmptyObject(attributeValues.metadata)){
                select.append($('<option></option>').attr('value', field).text(field));
            }
            else if(attributeValues.metadata[field])
            {
                var metadataArray = Array.isArray(element.metadata) ? element.metadata : [element.metadata];
                metadataArray.forEach(function(meta){
                    if(attributeValues.metadata[field][meta]){
                        select.append($('<option></option>').attr('value', field).text(field));
                    }
                });
            }
        });

        var onChangeNeeded = false;
        if(attributeValues.idToColumn){
            var initialField = attributeValues.idToColumn[element.id];
            if(initialField){
                setDropdownInitialValue(element.id, initialField);
                onChangeNeeded = true;
            }
        }
        return onChangeNeeded;
    }

    function setDropdownInitialValue (selectElementId, value) {
        $('#' + selectElementId + ' option[value="' + value + '"]').prop('selected', true);
    }

    return {
        setDropdownInitialValue: setDropdownInitialValue,

        getFieldNamesFromDropdown: function (selectElementId) {
            var optionsSelector = $('#' + selectElementId + ' option');
            return {
                data: $.map(optionsSelector, function (option) {
                    if (!option.value) {
                        return null;
                    }
                    return option.value;
                })
            };
        },

        populateAttributeDropdowns: function (attributeValues, dropDownElements, onChange) {
            var dropDownIdsArray = Array.isArray(dropDownElements) ? dropDownElements : [dropDownElements];
            var onChangedNeeded = false;
            dropDownIdsArray.forEach(function (element) {
                var select = $('#' + element.id);
                select.empty();
                $('#'+ element.id +' option:selected').removeAttr("selected");
                select.append($('<option></option>').attr('value', '').text('(Select Field)'));
                select.change(onChange);
                if(populateFieldValues(attributeValues, element)){
                    onChangedNeeded = true;
                }
            });
            if(onChangedNeeded){
                onChange();
            }
        }
    }
})();

neon.dropdown.Element = function(id, metadata){
    this.id = id;
    this.metadata = metadata;
};
