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

neon.toggle = (function () {

    function decorateOptionsPanel(selector, id) {
        var idString = '';
        if (arguments.length === 2) {
            idString = 'id="' + id + '" ';
        }
        $(selector).replaceWith(function () {
            return '<div ' + idString + 'class="options-bar"><div class="toggle"><img class="toggle-image"/><label class=options-label>Options</label></div>' + $(this)[0].outerHTML + '</div>';
        });
    }

    function configureToggle(selector) {
        $(".toggle").click(function () {
            $(selector).slideToggle("slow");

            if ($(".toggle-image").attr('src') === "img/arrow_down.png") {
                $(".toggle-image").attr('src', $(".toggle-image").attr('src').replace('_down', '_right'));
                $(".toggle").addClass("toggle-corners");

            } else {
                $(".toggle-image").attr('src', $(".toggle-image").attr('src').replace('_right', '_down'));
                $(".toggle").removeClass("toggle-corners");
            }
        });
    }

    function initToggleImage() {
        $(".toggle-image").attr("src", "img/arrow_down.png");
    }

    return {
        createOptionsPanel: function (selector, id) {
            $(document).ready(function () {
                decorateOptionsPanel(selector, id);
                configureToggle(selector);
                initToggleImage();
            });
        }
    }

})();

