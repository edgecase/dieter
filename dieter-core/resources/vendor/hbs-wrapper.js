var Element = {};
Element.firstChild = function () { return Element; };
Element.innerHTML = function () { return Element; };

var document = { createRange: false, createElement: function() { return Element; } };
var window = this;
this.document = document;

// Console
var console = window.console = {};
console.log = console.info = console.warn = console.error = function(){};

// jQuery
var jQuery = function() { return jQuery; };
jQuery.ready = function() { return jQuery; };
jQuery.inArray = function() { return jQuery; };
jQuery.jquery = "1.7.1";
var $ = jQuery;

function precompileEmber(templateString){
  return Ember.Handlebars.precompile(templateString).toString();
};

function precompileHandlebars(templateString){
  return Handlebars.precompile(templateString).toString();
};
