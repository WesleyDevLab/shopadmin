App.widget.TitularSelector = function (container, callback) {

  var selector = container.find(".js-titulares");

  var filter;

  var onSelect = callback || function () {};

  var initEventListeners = function () {
    container.find(".js-proveedor").change(function (event) {
      reset();
      filter = selector.autocomplete('option', 'source', "../proveedores/suggest");
    });

    container.find(".js-shopper").change(function (event) {
      reset();
      filter = selector.autocomplete('option', 'source', "../services/shoppers/suggest");
    });
  };

  var reset = function () {
    selector.val('');
    container.find(".js-titular-id").val('');
  };

  var highlight = function (value, term) {
    var matcher = new RegExp("(" + $.ui.autocomplete.escapeRegex(term) + ")", "ig");
    return value.replace(matcher, "<strong>$1</strong>");
  };

  return {
    render: function () {
      var me = this;
      var suggestEndpoint = "../proveedores/suggest";
      var shopperSelected = container.find(".js-shopper").prop("checked");
      if (shopperSelected) {
        suggestEndpoint = "../services/shoppers/suggest";
      }
      filter = selector.autocomplete({
        source: suggestEndpoint,
        minLength: 2,
        select: function(event, ui) {
          if (ui.item.name) {
            selector.val(ui.item.name);
          } else {
            selector.val(ui.item.description);
          }
          container.find(".js-titular-id").val(ui.item.id);
          onSelect(me.getTitularSelected());
          return false;
        }
      });
      filter.data( "ui-autocomplete" )._renderItem = function(ul, item) {
        var display = ((item.name) ? item.name : item.description);
        display = highlight(display, selector.val());
        return $("<li>")
          .append("<a>" + display + "</a>")
          .appendTo(ul);
      };

      initEventListeners();
    },
    getTitularSelected: function() {
      current = null;
      if (container.find(".js-titular-id").val()) {
        current = {
          id: container.find(".js-titular-id").val(),
          tipo: container.find("input:radio[name=tipoTitular]:checked").val(),
          name: selector.val()
        }
      }
      return current;
    }
  };
}
