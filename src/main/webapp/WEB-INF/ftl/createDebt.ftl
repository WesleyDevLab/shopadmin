<!DOCTYPE html>
<html>
  <head>
    <#import "/spring.ftl" as spring />
    <meta charset="utf-8">
    <title>Shopnchek</title>
    <meta http-equiv="cleartype" content="on">

    <link rel="stylesheet" href="<@spring.url '/css/jquery-ui/jquery-ui.css'/>">

    <link rel="stylesheet" href="<@spring.url '/css/base.css'/>">
    <link rel="stylesheet" href="<@spring.url '/css/shop.css'/>">
    <link rel="stylesheet" href="<@spring.url '/css/custom.css'/>">

    <script src="<@spring.url '/script/jquery.js'/>"></script>
    <script src="<@spring.url '/script/jquery-ui.js'/>"></script>
    <script src="<@spring.url '/script/pure.min.js'/>"></script>
    <script src="<@spring.url '/script/livevalidation.js'/>"></script>

    <#assign actionUrl = "create" />
    <#assign readOnly = model["readOnly"]!true />
    <#if model["debt"]??>
      <#assign debt = model["debt"] />
      <#assign actionUrl = "../update/${debt.id?c}" />
    </#if>

    <script type="text/javascript">

      window.App = window.App || {};

      App.widget = App.widget || {};

App.widget.AdicionalEditor = function (container) {

  var rowIndex = 1;

  var branchSelector = container.find(".js-sucursales");

  var itemTemplate = container.find(".js-items tbody tr");

  var shopperSelector = new App.widget.ShopperSelector(
      container.find(".js-shopper-selector"), false, "<@spring.url '/services/shoppers/suggest' />");

  var validations = [];

  var initValidations = function() {
    var fechaValidation = new LiveValidation("fecha");
    fechaValidation.add(Validate.Presence, {
      failureMessage: "La fecha del adicional es obligatoria"
    });
    validations.push(fechaValidation);

    /*var importeValidation = new LiveValidation("importe");
    importeValidation.add(Validate.Presence, {
      failureMessage: "El importe es obligatorio"
    });
    importeValidation.add(Validate.Numericality, {
      minimum: 0,
      notANumberMessage: "El importe debe ser un numero mayor que 0",
      tooLowMessage: "El importe debe ser un numero mayor que 0"
    });
    validations.push(importeValidation);*/

    /*var tipoPagoValidation = new LiveValidation("tipoPago");
    tipoPagoValidation.add(Validate.Custom, {
      against: function (value, args) {
        var tipoPago = container.find("select[name='tipoPagoId']").val();
        var isValid = true;
        if (tipoPago !== '3' && itemId === '') {
          isValid = jQuery.inArray(tipoPago, adicionales) === -1;
        }
        return isValid;
      },
      failureMessage: "Este tipo de pago no esta disponible para el mismo dia"
    });*/
  };

  var initEventListeners = function() {
    container.find(".js-fecha-visita" ).datepicker({
      dateFormat: 'dd/mm/yy',
      onSelect: function(dateText, datePicker) {
        $(this).attr('value', dateText);
      }
    });

    container.find(".js-add").click(function () {
      //resetVisitaValidations();
      if (LiveValidation.massValidate(validations)) {
        var items = [];
        container.find(".js-items tbody tr").each(function (index) {
          var row = jQuery(this);
          items.push({
            tipoPago: row.find("[name='tipoPago']").val(),
            importe: row.find("[name='importe']").val(),
            observacion: row.find("[name='observaciones']").val()
          });
        });
        jQuery.ajax({
          headers: { 
            'Accept': 'application/json',
            'Content-Type': 'application/json' 
          },
          url: "${actionUrl}",
          dataType: 'json',
          data: JSON.stringify({
            "shopperDni": container.find(".js-shopper-dni").val(),
            "clientId": container.find(".js-client-id").val(),
            "clientDescription": container.find(".js-clients").val(),
            "branchId": container.find(".js-sucursales").val(),
            "branchDescription": container.find(".js-sucursal-description").val(),
            "fecha": container.find(".js-fecha-visita").val(),
            "items": items
          }),
          method: 'POST'
        }).done(function (data) {
          location.href = 'list';
        })
      }
    });

    container.find("#item-1 .js-remove-item").click(function (event) {
      event.preventDefault();
      jQuery('#item-1').remove();
    });

    container.find(".js-add-item").click(function () {
      rowIndex++;
      container.find('.js-items tr:last').after('<tr id="item-' + rowIndex + '">' + itemTemplate.html() + '</tr>');
      container.find("#item-" + rowIndex + " .js-remove-item").click(function () {
        event.preventDefault();
        jQuery(this).parent('td').parent('tr').remove();
      });
    });
  };

  var updateBranchs = function (branchs) {
    branchSelector.empty();
    branchSelector.append(jQuery("<option>", {
      value: "",
      text: "Seleccione una sucursal"
    }));
    jQuery.each(branchs, function (index, branch) {
      branchSelector.append(jQuery("<option>", {
        value: branch.id,
        text: branch.address
      }));
    });
    branchSelector.prop("disabled", false);
  }

  return {
    render: function() {
      shopperSelector.render();

      var clientsSelector = container.find(".js-clients").autocomplete({
        source: "<@spring.url '/services/client/findByName' />",
        minLength: 2,
        focus: function(event, ui) {
          clientsSelector.val(ui.item.name);
          return false;
        },
        select: function(event, ui) {
          clientsSelector.val(ui.item.name);
          container.find(".js-client-id").val(ui.item.id);
          updateBranchs(ui.item.branchs);
          return false;
        }
      });
      clientsSelector.data("ui-autocomplete")._renderItem = function(ul, item) {
        return $("<li>")
          .append("<a>" + item.name + "</a>")
          .appendTo(ul);
      };

      initEventListeners();
      initValidations();
    }
  }

}

      jQuery(document).ready(function() {
        var editorContainer = jQuery(".js-editor-adicional");
        var editor = App.widget.AdicionalEditor(editorContainer);
        editor.render();
      });

    </script>

    <script src="<@spring.url '/script/ShopperSelector.js'/>"></script>

  </head>
  <body>
    <#include "header.ftl" />

    <div class="container-box-plantilla">
      <h2 class="container-tit">Autorizaci&oacute;n de adicionales</h2>

      <form action="${actionUrl}" method="POST" class="form-shop form-shop-big js-editor-adicional">
       <!--div role="alert" class="form-error-txt" aria-hidden="false"><i class="ch-icon-remove"></i>
          <div class="ch-popover-content">
            Revisa los datos. Debes completar campos "Número" y "Factura".
          </div>
        </div-->
        <h2 class="subtitulo">Item a pagar</h2>
        <!-- FILA 1 -->
        <div class="cell js-visita">
          <div class="box-gray">
            <fieldset>
              <input type="hidden" name="tipoItem" value="${(debt.tipoItem)!'manual'}" />
              <div class="form-shop-row js-shopper-selector">
                <label for="shopper">Shopper</label>
                <input id="shopper" type="text" value="${(debt.shopper.name)!''}" class="item-field js-shopper" <#if readOnly>disabled="true"</#if> />
                <a id="clear-shopper" href="#" class="clear js-clear">limpiar</a>
                <input type="hidden" name="shopperId" value="${(debt.shopper.id?c)!''}" class="js-shopper-id" />
                <input type="hidden" name="shopperDni" value="${(debt.shopperDni)!''}" class="js-shopper-dni" />
              </div>
              <div class="form-shop-row">
                <label for="client">Cliente</label>
                <input id="client" type="text" name="clientDescription" value="${(debt.client.name)!(debt.clientDescription)!''}" class="item-field js-clients" <#if readOnly>disabled="true"</#if>/>
                <input type="hidden" name="clientId" value="${(debt.client.id)!''}" class="js-client-id" />
              </div>
              <div class="form-shop-row">
                <label for="sucursal">Sucursal</label>
                <select id="sucursal" name="branchId" class="item-field js-sucursales" <#if readOnly>disabled="true"</#if>>
                <#if debt?? && debt.branch??>
                  <option value="${debt.branch.id?c}">${debt.branch.address}</option>
                </#if>
                </select>
              </div>
              <div class="form-shop-row">
                <label>&nbsp;</label>
                <input type="text" name="branchDescription" value="${(debt.branchDescription)!''}" class="item-field js-sucursal-description" <#if readOnly>disabled="true"</#if>/>
              </div>
              <div class="form-shop-row">
                <label for="fecha">Fecha de la visita:</label>
                <input type="text" id="fecha" name="fecha" value="${(debt.fecha?string('dd/MM/yyyy'))!""}" class="item-field js-date js-fecha-visita" <#if readOnly>disabled="true"</#if>/>
              </div>
              <table summary="Lista de items" class="table-form js-items">
                <thead>
                  <tr>
                    <th scope="col" style="width: 20%">Tipo de pago</th>
                    <th scope="col" style="width: 20%">Importe</th>
                    <th scope="col" style="width: 55%">Observaciones</th>
                    <th scope="col" style="width: 5%">&nbsp;</th>
                  </tr>
                </thead>
                <tbody>
                  <tr id="item-1">
                    <td>
                      <select name="tipoPago" class="item-field">
                      <#list model["tiposPago"] as tipoPago>
                        <option value="${tipoPago}">${tipoPago.description}</option>
                      </#list>
                      </select>
                    </td>
                    <td>
                      <input type="text" name="importe" value="0" class="item-field" />
                    </td>
                    <td>
                      <input type="text" name="observaciones" value="" class="item-field" />
                    </td>
                    <td><a href="#" class="js-remove-item">borrar</a></td>
                  </tr>
                </tbody>
              </table>
              <input type="button" class="btn-shop-small js-add-item" value="Agregar item" />
            </fieldset>
          </div>
          <ul class="action-columns">
            <li>
              <#if debt?? && readOnly>
                <a href="<@spring.url '/debt/edit/${debt.id?c}'/>" class="btn-shop-small">Editar</a>
              <#else>
                <input type="button" class="btn-shop-small js-add" value="Guardar" />
              </#if>
            </li>
          </ul>
        </div>
        <!-- FIN FILA 1 -->
      </form>
    </div>
  </body>
</html>