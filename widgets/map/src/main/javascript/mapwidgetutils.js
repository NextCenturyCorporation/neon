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
neon.mapWidgetUtils = (function (){


    function getLatField() {
        return $('#latitude option:selected').val();
    }

    function getLonField() {
        return $('#longitude option:selected').val();
    }

    function getSizeByField() {
        return $('#size-by option:selected').val();
    }

    function getColorByField() {
        return $('#color-by option:selected').val();
    }

    function getLayer() {
        return $("input[name='layer-group']:checked").val();
    }

    function getDropdownSelectedValue(dropdownName){
        return $('#' + dropdownName + ' option:selected').val();
    }

    function addDropdownChangeListener(dropdownName, onChange){
        $('#' + dropdownName).change(function (){
            onChange.call(this, getDropdownSelectedValue(dropdownName));
        });
    }

    function setLayerChangeListener(onChange){
        $("input[name='layer-group']").change(onChange);
    }

    function setLayer(selectElementId) {
        $('#' + selectElementId).attr('checked', true);
    }

    return {
        addDropdownChangeListener: addDropdownChangeListener,
        setLayerChangeListener: setLayerChangeListener,
        getLatitudeField: getLatField,
        getLongitudeField: getLonField,
        getSizeByField: getSizeByField,
        getColorByField: getColorByField,
        getLayer: getLayer,
        setLayer: setLayer,
        latitudeAndLongitudeAreSelected : function(){
            return (getLatField() && getLonField());
        }
    };

})();