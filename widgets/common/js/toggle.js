

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

