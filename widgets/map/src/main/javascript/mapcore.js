var coreMap = coreMap || {};

coreMap.Map = function(selectorText, opts){
    this.selectorText = selectorText;

    $(selectorText).append("ASDF");
};