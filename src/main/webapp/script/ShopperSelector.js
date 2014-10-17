App.widget.ShopperSelector = function (container, skipValidation) {

  var selector = container.find(".js-shopper");

  var clearButton = container.find('.js-clear');

  var currentShopper = null;

  var highlight = function (value, term) {
    var matcher = new RegExp("(" + $.ui.autocomplete.escapeRegex(term) + ")", "ig");
    return value.replace(matcher, "<strong>$1</strong>");
  };

  var initValidators = function () {
    if (!skipValidation) {
      var shopperValidation = new LiveValidation("shopper",
          {onlyOnSubmit: true, insertAfterWhatNode: 'clear-shopper'});
      shopperValidation.add(Validate.Presence, {
        failureMessage: "Debe seleccionar un shopper"
      });
    }
  };

  var initEventListeners = function () {
    var filter = selector.autocomplete({
      source: "../services/shoppers/suggest",
      minLength: 2,
      select: function(event, ui) {
        currentShopper = ui.item;
        selector.val(ui.item.name);
        container.find(".js-shopper-id").val(ui.item.id);
        container.find(".js-shopper-dni").val(ui.item.dni);
        return false;
      }
    });
    filter.data( "ui-autocomplete" )._renderItem = function(ul, item) {
      var display = item.name;
      display = highlight(display, selector.val());
      return $("<li>")
        .append("<a>" + display + "</a>")
        .appendTo(ul);
    };
    clearButton.click(function (event) {
      event.preventDefault();
      reset();
    });
  };

  var reset = function () {
    currentShopper = null;
    selector.val('');
    container.find(".js-shopper-id").val('');
    container.find(".js-shopper-dni").val('');
  };

  return {
    render: function () {
      initEventListeners();
      initValidators();
    },
    getCurrentShopper: function () {
      return currentShopper;
    },
    reset: function () {
      reset();
    }
  };
};
