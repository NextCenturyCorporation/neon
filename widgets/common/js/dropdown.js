/*
 * Copyright 2013 Next Century Corporation
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
