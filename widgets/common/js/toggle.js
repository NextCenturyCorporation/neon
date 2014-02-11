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

neon.toggle = (function () {

    function decorateOptionsPanel(selector, id) {
        var idString = '';
        if (id) {
            idString = 'id="' + id + '" ';
        }
        $(selector).replaceWith(function () {
            return '<div ' + idString + 'class="options-bar"><div id="options-toggle-id" class="toggle"><label class="options-label">Options</label><img src="img/arrow.png" class="toggle-image"/></div>' + $(this)[0].outerHTML + '</div>';
        });
    }

    return {
        createOptionsPanel: function (selector, id) {
            $(function() {
                decorateOptionsPanel(selector, id);
                $("#options-toggle-id").click(function() {
                    $(selector).slideToggle("slow");
                });
            });
        }
    }
})();

