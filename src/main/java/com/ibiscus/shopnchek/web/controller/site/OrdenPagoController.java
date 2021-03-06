package com.ibiscus.shopnchek.web.controller.site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ibiscus.shopnchek.application.ResultSet;
import com.ibiscus.shopnchek.application.order.AsociarMedioPagoCommand;
import com.ibiscus.shopnchek.application.order.GetOrderCommand;
import com.ibiscus.shopnchek.application.order.ItemsOrdenService;
import com.ibiscus.shopnchek.application.order.OrderDto;
import com.ibiscus.shopnchek.application.order.PayOrderCommand;
import com.ibiscus.shopnchek.application.order.RemoveItemOrderCommand;
import com.ibiscus.shopnchek.application.order.SaveOrderCommand;
import com.ibiscus.shopnchek.application.order.SearchOrderDtoCommand;
import com.ibiscus.shopnchek.application.order.TransitionOrderCommand;
import com.ibiscus.shopnchek.application.shopmetrics.ImportService;
import com.ibiscus.shopnchek.domain.admin.AsociacionMedioPago;
import com.ibiscus.shopnchek.domain.admin.ItemOrden;
import com.ibiscus.shopnchek.domain.admin.MedioPago;
import com.ibiscus.shopnchek.domain.admin.OrdenPago;
import com.ibiscus.shopnchek.domain.admin.OrderRepository;
import com.ibiscus.shopnchek.domain.admin.OrderState;
import com.ibiscus.shopnchek.domain.admin.Proveedor;
import com.ibiscus.shopnchek.domain.admin.ProveedorRepository;
import com.ibiscus.shopnchek.domain.admin.Shopper;
import com.ibiscus.shopnchek.domain.admin.ShopperRepository;
import com.ibiscus.shopnchek.domain.debt.TipoPago;
import com.ibiscus.shopnchek.domain.security.User;

@Controller
@RequestMapping("/orden")
public class OrdenPagoController {

  /** Repository of orders. */
  @Autowired
  private OrderRepository orderRepository;

  /** Repository of shoppers. */
  @Autowired
  private ShopperRepository shopperRepository;

  /** Repository of proveedores. */
  @Autowired
  private ProveedorRepository proveedorRepository;

  /** Repository of items. */
  @Autowired
  private ItemsOrdenService itemsOrdenService;

  /** Service to import external data. */
  @Autowired
  private ImportService importService;

  @Autowired
  private SaveOrderCommand saveOrderCommand;

  @Autowired
  private GetOrderCommand getOrderCommand;

  @Autowired
  private SearchOrderDtoCommand searchOrderDtoCommand;

  @Autowired
  private PayOrderCommand payOrderCommand;

  @Autowired
  private TransitionOrderCommand transitionOrderCommand;

  @Autowired
  private RemoveItemOrderCommand removeItemOrderCommand;

  @Autowired
  private AsociarMedioPagoCommand asociarMedioPagoCommand;

  @RequestMapping(value="/")
  public String index(@ModelAttribute("model") final ModelMap model) {
    /*User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    model.addAttribute("orderStates", orderRepository.findOrderStates());
    model.addAttribute("mediosPago", orderRepository.findMediosPago());
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, -4);
    model.addAttribute("fechaDesde", calendar.getTime());*/
    return "redirect:create";
  }

  @RequestMapping(value="/create")
  public String create(@ModelAttribute("model") final ModelMap model) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    return "createOrder";
  }

  @RequestMapping(value="/edit/{orderId}")
  public String edit(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    getOrderCommand.setNumero(orderId);
    OrdenPago ordenPago = getOrderCommand.execute();
    model.addAttribute("ordenPago", ordenPago);

    if (ordenPago.getTipoProveedor() == 1) {
      Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", shopper.getName());
    } else {
      Proveedor proveedor = proveedorRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", proveedor.getDescription());
    }

    return "createOrder";
  }

  @RequestMapping(value="/{orderId}")
  public String get(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    getOrderCommand.setNumero(orderId);
    OrdenPago ordenPago = getOrderCommand.execute();
    model.addAttribute("ordenPago", ordenPago);

    if (ordenPago.getTipoProveedor() == 1) {
      Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", shopper.getName());
    } else {
      Proveedor proveedor = proveedorRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", proveedor.getDescription());
    }

    model.addAttribute("mediosPago", orderRepository.findMediosPago());
    model.addAttribute("tiposPago",TipoPago.values());
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, -4);
    model.addAttribute("fechaDesde", calendar.getTime());
    return "orderItems";
  }

  @RequestMapping(value="create", method=RequestMethod.POST)
  public String createOrden(@ModelAttribute("model") final ModelMap model,
      int tipoTitular, int titularId, String tipoFactura,
      @DateTimeFormat(pattern="dd/MM/yyyy") Date fechaPago,
      @NumberFormat(style=Style.NUMBER, pattern="#,##") double iva, String numeroFactura,
      String localidad, String observaciones, String observacionesShopper) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    saveOrderCommand.setNumero(null);
    saveOrderCommand.setTipoProveedor(tipoTitular);
    saveOrderCommand.setProveedor(titularId);
    saveOrderCommand.setTipoFactura(tipoFactura);
    saveOrderCommand.setFechaPago(fechaPago);
    saveOrderCommand.setIva(iva);
    saveOrderCommand.setNumeroFactura(numeroFactura);
    saveOrderCommand.setLocalidad(localidad);
    saveOrderCommand.setObservaciones(observaciones);
    saveOrderCommand.setObservacionesShopper(observacionesShopper);

    OrdenPago ordenPago = saveOrderCommand.execute();
    model.addAttribute("ordenPago", ordenPago);

    Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
    model.addAttribute("titular", shopper);
    return "redirect:" + ordenPago.getNumero();
  }

  @RequestMapping(value="save", method=RequestMethod.POST)
  public String update(@ModelAttribute("model") final ModelMap model,
      long numeroOrden, int tipoTitular, int titularId, String tipoFactura,
      @DateTimeFormat(pattern="dd/MM/yyyy") Date fechaPago,
      @NumberFormat(style=Style.NUMBER, pattern="#,##") double iva,
      String numeroFactura, String localidad, String observaciones,
      String observacionesShopper) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    saveOrderCommand.setNumero(numeroOrden);
    saveOrderCommand.setTipoProveedor(tipoTitular);
    saveOrderCommand.setProveedor(titularId);
    saveOrderCommand.setTipoFactura(tipoFactura);
    saveOrderCommand.setFechaPago(fechaPago);
    saveOrderCommand.setIva(iva);
    saveOrderCommand.setNumeroFactura(numeroFactura);
    saveOrderCommand.setLocalidad(localidad);
    saveOrderCommand.setObservaciones(observaciones);
    saveOrderCommand.setObservacionesShopper(observacionesShopper);

    OrdenPago ordenPago = saveOrderCommand.execute();
    model.addAttribute("ordenPago", ordenPago);

    return "redirect:" + numeroOrden;
  }

  @RequestMapping(value="/{orderId}/item/{itemId}", method=RequestMethod.DELETE)
  public @ResponseBody boolean deleteItem(@PathVariable long orderId, @PathVariable long itemId) {
    removeItemOrderCommand.setNumero(orderId);
    removeItemOrderCommand.setItemId(itemId);
    return removeItemOrderCommand.execute();
  }

  @RequestMapping(value="asociarMedioPago", method=RequestMethod.POST)
  public @ResponseBody String asociarMedioPago(@ModelAttribute("model") final ModelMap model,
      long numeroOrden, int medioPagoId) {

    asociarMedioPagoCommand.setNumeroOrden(numeroOrden);
    asociarMedioPagoCommand.setMedioPagoId(medioPagoId);
    MedioPago medioPago = asociarMedioPagoCommand.execute();
    return medioPago.getDescription();
  }

  @RequestMapping(value="/pay/{orderId}", method = RequestMethod.POST)
  public String payOrder(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId, long medioPagoId,
      @RequestParam(required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fechaCheque,
      @RequestParam(required = false) String numeroChequera,
      @RequestParam(required = false) String numeroCheque,
      @RequestParam(required = false) String transferId,
      String observaciones, String observacionesShopper, Integer state) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    payOrderCommand.setNumero(orderId);
    payOrderCommand.setStateId(state);
    payOrderCommand.setMedioPagoId(medioPagoId);
    payOrderCommand.setNumeroChequera(numeroChequera);
    payOrderCommand.setNumeroCheque(numeroCheque);
    payOrderCommand.setFechaCheque(fechaCheque);
    payOrderCommand.setIdTransferencia(transferId);
    payOrderCommand.setObservaciones(observaciones);
    payOrderCommand.setObservacionesShopper(observacionesShopper);
    OrdenPago ordenPago = payOrderCommand.execute();
    model.addAttribute("ordenPago", ordenPago);

    return "redirect:../" + orderId;
  }

  @RequestMapping(value="/cancel/{orderId}", method = RequestMethod.POST)
  public @ResponseBody boolean cancel(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    transitionOrderCommand.setNumero(orderId);
    transitionOrderCommand.setStateId(OrderState.ANULADA);
    transitionOrderCommand.execute();

    return true;
  }

  @RequestMapping(value="/close/{orderId}", method = RequestMethod.POST)
  public @ResponseBody boolean close(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    transitionOrderCommand.setNumero(orderId);
    transitionOrderCommand.setStateId(OrderState.CERRADA);
    transitionOrderCommand.execute();

    return true;
  }

  @RequestMapping(value="/open/{orderId}", method = RequestMethod.POST)
  public @ResponseBody boolean open(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    transitionOrderCommand.setNumero(orderId);
    transitionOrderCommand.setStateId(OrderState.ABIERTA);
    transitionOrderCommand.execute();

    return true;
  }

  @RequestMapping(value="/verified/{orderId}", method = RequestMethod.POST)
  public @ResponseBody boolean verified(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    transitionOrderCommand.setNumero(orderId);
    transitionOrderCommand.setStateId(OrderState.VERIFICADA);
    transitionOrderCommand.execute();

    return true;
  }

  @RequestMapping(value="/pause/{orderId}", method = RequestMethod.POST)
  public @ResponseBody boolean pause(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    transitionOrderCommand.setNumero(orderId);
    transitionOrderCommand.setStateId(OrderState.EN_ESPERA);
    transitionOrderCommand.execute();

    return true;
  }

  @RequestMapping(value="getAsociacionMedioPago")
  public @ResponseBody AsociacionMedioPago getAsociacionMedioPago(
      @ModelAttribute("model") final ModelMap model,
      int tipoProveedor, int titularId) {

    AsociacionMedioPago asociacion = orderRepository.findAsociacion(
        tipoProveedor, titularId);
    return asociacion;
  }

  @RequestMapping(value="/searchByNumber")
  public String searchByNumber(@ModelAttribute("model") final ModelMap model,
      final Long numeroOrden) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    model.addAttribute("orderStates", orderRepository.findOrderStates());

    getOrderCommand.setNumero(numeroOrden);
    OrdenPago order = getOrderCommand.execute();
    model.put("numberOrden", numeroOrden);
    model.put("result", new ResultSet<OrderDto>(1, 25, new ArrayList<OrderDto>(), 0));
    model.put("page", 1);
    model.put("pageSize", 25);

    if (order != null) {
      return "redirect:" + numeroOrden;
    } else {
      return "buscadorOrdenPago";
    }
  }

  @RequestMapping(value="/search")
  public String search(@ModelAttribute("model") final ModelMap model,
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "fechaPago") String orderBy,
      @RequestParam(required = false, defaultValue = "false") Boolean ascending,
      Long numeroOrden, Integer tipoTitular, Integer titularId, String shopper,
      String shopperDni, Long shopperId, String numeroCheque, Long estadoId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    model.addAttribute("orderStates", orderRepository.findOrderStates());

    if (titularId != null) {
      if (tipoTitular.equals(1)) {
        Shopper shopperTitular = shopperRepository.get(titularId);
        model.addAttribute("titularNombre", shopperTitular.getName());
      } else {
        Proveedor proveedor = proveedorRepository.get(titularId);
        model.addAttribute("titularNombre", proveedor.getDescription());
      }
    }
    searchOrderDtoCommand.setPage(page);
    searchOrderDtoCommand.setPageSize(25);
    searchOrderDtoCommand.setOrderBy(orderBy, ascending);
    searchOrderDtoCommand.setTipoTitular(tipoTitular);
    searchOrderDtoCommand.setTitularId(titularId);
    searchOrderDtoCommand.setDniShopper(shopperDni);
    searchOrderDtoCommand.setNumeroCheque(numeroCheque);
    searchOrderDtoCommand.setStateId(estadoId);
    ResultSet<OrderDto> rsOrdenes = searchOrderDtoCommand.execute();

    model.put("result", rsOrdenes);
    model.put("page", page);
    model.put("pageSize", 25);
    model.put("orderBy", orderBy);
    model.put("ascending", ascending);
    model.put("numeroOrden", numeroOrden);
    model.put("tipoTitular", tipoTitular);
    model.put("titularId", titularId);
    model.put("state", estadoId);
    model.put("shopperDni", shopperDni);
    model.put("shopperId", shopperId);
    model.put("shopper", shopper);
    model.put("numeroCheque", numeroCheque);

    return "buscadorOrdenPago";
  }

  @RequestMapping(value="/caratula/{orderId}")
  public String getCaratula(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    Set<String> clients = new HashSet<String>();
    OrdenPago ordenPago = orderRepository.get(orderId);
    for (ItemOrden itemOrden : ordenPago.getItems()) {
      clients.add(itemOrden.getCliente());
    }
    model.addAttribute("ordenPago", ordenPago);
    model.addAttribute("clients", clients);
    if (ordenPago.getTipoProveedor() == 1) {
      Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", shopper.getName() + " "
          + shopper.getUsername());
    } else {
      Proveedor proveedor = proveedorRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", proveedor.getDescription());
    }
    return "caratula";
  }

  @RequestMapping(value="/remito/{orderId}")
  public String getRemito(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    getOrderCommand.setNumero(orderId);
    OrdenPago ordenPago = getOrderCommand.execute();
    model.addAttribute("ordenPago", ordenPago);

    if (ordenPago.getTipoProveedor() == 1) {
      Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", shopper.getName());
    } else {
      Proveedor proveedor = proveedorRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", proveedor.getDescription());
      model.addAttribute("bancoAsociado", proveedor.getBanco());
    }
    return "remito";
  }

  @RequestMapping(value="/printdetail/{orderId}")
  public String printDetail(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    getOrderCommand.setNumero(orderId);
    OrdenPago ordenPago = getOrderCommand.execute();
    model.addAttribute("ordenPago", ordenPago);

    if (ordenPago.getTipoProveedor() == 1) {
      Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", shopper.getName());
    } else {
      Proveedor proveedor = proveedorRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", proveedor.getDescription());
    }
    return "printDetail";
  }

  @RequestMapping(value="/printshopper/{orderId}")
  public String printShopper(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);

    getOrderCommand.setNumero(orderId);
    OrdenPago ordenPago = getOrderCommand.execute();
    model.addAttribute("ordenPago", ordenPago);

    return "printShopper";
  }

  @RequestMapping(value="/export")
  public String export(@ModelAttribute("model") final ModelMap model) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    return "exportOrden";
  }

  @RequestMapping(value="/download")
  public void download(HttpServletResponse response, Date desde, Date hasta) {
    String fileName = "ordenesDePago.xls";
    response.setContentType("application/vnd.openxmlformats-officedocument."
        + "spreadsheetml.sheet");
    String headerKey = "Content-Disposition";
    String headerValue = String.format("attachment; filename=\"%s\"",
            fileName);
    response.setHeader(headerKey, headerValue);

    try {
      importService.exportOrdenes(response.getOutputStream(), desde, hasta);
    } catch (IOException e) {
      throw new RuntimeException("Cannot write the XLS file", e);
    }
  }
}
