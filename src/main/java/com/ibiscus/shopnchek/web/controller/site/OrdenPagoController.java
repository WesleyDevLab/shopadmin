package com.ibiscus.shopnchek.web.controller.site;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ibiscus.shopnchek.application.orden.ItemsOrdenService;
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

  @RequestMapping(value="/")
  public String index(@ModelAttribute("model") final ModelMap model) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    model.addAttribute("orderStates", orderRepository.findOrderStates());
    model.addAttribute("mediosPago", orderRepository.findMediosPago());
    return "ordenPago";
  }

  @RequestMapping(value="/{orderId}")
  public String get(@ModelAttribute("model") final ModelMap model,
      @PathVariable long orderId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    model.addAttribute("mediosPago", orderRepository.findMediosPago());
    OrdenPago ordenPago = orderRepository.get(orderId);
    for (ItemOrden itemOrden : ordenPago.getItems()) {
      Shopper itemShopper = shopperRepository.findByDni(itemOrden.getShopperDni());
      itemOrden.updateShopper(itemShopper);
    }
    model.addAttribute("ordenPago", ordenPago);
    if (ordenPago.getTipoProveedor() == 1) {
      Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", shopper.getName());
    } else {
      Proveedor proveedor = proveedorRepository.get(ordenPago.getProveedor());
      model.addAttribute("titularNombre", proveedor.getDescription());
    }

    Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
    model.addAttribute("titular", shopper);
    AsociacionMedioPago asociacion = orderRepository.findAsociacion(
        ordenPago.getTipoProveedor(), ordenPago.getProveedor());
    if (asociacion != null) {
      MedioPago medioPago = orderRepository.getMedioPago(
          asociacion.getMedioPago());
      model.addAttribute("medioPagoPredeterminado", medioPago.getDescription());
    }
    List<OrderState> states = new ArrayList<OrderState>();
    List<OrderState> availableStates = orderRepository.findOrderStates();
    if (ordenPago.getEstado().getId() == 3
        || ordenPago.getEstado().getId() == 4) {
      for (OrderState state : availableStates) {
        if (state.getId() == 3 || state.getId() == 4) {
          states.add(state);
        }
      }
    } else if (ordenPago.getEstado().getId() == 5) {
      for (OrderState state : availableStates) {
        if (state.getId() == 1 || state.getId() == 5) {
          states.add(state);
        }
      }
    } else if (ordenPago.getEstado().getId() == 2) {
      for (OrderState state : availableStates) {
        if (state.getId() > 1 && state.getId() < 5) {
          states.add(state);
        }
      }
    } else if (ordenPago.getEstado().getId() == 6) {
      for (OrderState state : availableStates) {
        if (state.getId() == 6) {
          states.add(state);
        }
      }
    } else {
      states.addAll(availableStates);
    }
    model.addAttribute("orderStates", states);

    return "ordenPago";
  }

  @RequestMapping(value="create", method=RequestMethod.POST)
  public String createOrden(@ModelAttribute("model") final ModelMap model,
      int tipoTitular, int titularId, String tipoFactura, Date fechaPago,
      long estadoId, long medioPagoId, double iva, String facturaNumero,
      Date fechaCheque, String chequeraNumero, String chequeNumero,
      String transferId, String localidad, String observaciones,
      String observacionesShopper) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    model.addAttribute("orderStates", orderRepository.findOrderStates());
    model.addAttribute("mediosPago", orderRepository.findMediosPago());

    OrderState state = orderRepository.getOrderState(estadoId);
    MedioPago medioPago = orderRepository.getMedioPago(medioPagoId);
    OrdenPago ordenPago = new OrdenPago(tipoTitular, titularId,
        tipoFactura, fechaPago, state, medioPago, iva);
    ordenPago.update(tipoTitular, titularId, tipoFactura, fechaPago, state,
        medioPago, iva, facturaNumero, fechaCheque, chequeraNumero,
        chequeNumero, transferId, localidad, observaciones,
        observacionesShopper);

    long numeroOrden = orderRepository.save(ordenPago);
    ordenPago = orderRepository.get(numeroOrden);
    model.addAttribute("ordenPago", ordenPago);
    Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
    model.addAttribute("titular", shopper);
    return "redirect:" + numeroOrden;
  }

  @RequestMapping(value="save", method=RequestMethod.POST)
  public String update(@ModelAttribute("model") final ModelMap model,
      int numeroOrden, int tipoTitular, int titularId, String tipoFactura,
      Date fechaPago, long estadoId, long medioPagoId, double iva,
      String numeroFactura, Date fechaCheque, String numeroChequera,
      String numeroCheque, String transferId, String localidad,
      String observaciones, String observacionesShopper) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    model.addAttribute("orderStates", orderRepository.findOrderStates());
    model.addAttribute("mediosPago", orderRepository.findMediosPago());

    OrderState state = orderRepository.getOrderState(estadoId);
    MedioPago medioPago = orderRepository.getMedioPago(medioPagoId);

    OrdenPago ordenPago = orderRepository.get(numeroOrden);
    ordenPago.update(tipoTitular, titularId, tipoFactura, fechaPago,
        state, medioPago, iva, numeroFactura, fechaCheque, numeroChequera,
        numeroCheque, transferId, localidad, observaciones,
        observacionesShopper);
    orderRepository.update(ordenPago);

    model.addAttribute("ordenPago", ordenPago);
    Shopper shopper = shopperRepository.get(ordenPago.getProveedor());
    model.addAttribute("titular", shopper);
    return "redirect:" + numeroOrden;
  }

  @RequestMapping(value="/{orderId}/item/{itemId}", method=RequestMethod.DELETE)
  public @ResponseBody boolean deleteItem(@PathVariable long orderId, @PathVariable long itemId) {
    OrdenPago ordenPago = orderRepository.get(orderId);
    ItemOrden itemToRemove = null;
    for (ItemOrden itemOrden : ordenPago.getItems()) {
      if (itemOrden.getId() == itemId) {
        itemToRemove = itemOrden;
      }
    }
    ordenPago.getItems().remove(itemToRemove);
    orderRepository.removeItem(itemToRemove);
    return true;
  }

  @RequestMapping(value="asociarMedioPago", method=RequestMethod.POST)
  public @ResponseBody String asociarMedioPago(
      @ModelAttribute("model") final ModelMap model,
      int tipoProveedor, int titularId, int medioPagoId) {

    MedioPago medioPago = orderRepository.getMedioPago(medioPagoId);
    AsociacionMedioPago asociacion = orderRepository.findAsociacion(
        tipoProveedor, titularId);
    if (asociacion == null) {
      asociacion = new AsociacionMedioPago(tipoProveedor, titularId,
          medioPagoId);
      orderRepository.save(asociacion);
    } else {
      asociacion.update(medioPagoId);
      orderRepository.update(asociacion);
    }
    return medioPago.getDescription();
  }

  @RequestMapping(value="/search")
  public String search(@ModelAttribute("model") final ModelMap model,
      Long numeroOrden, Long tipoTitular, Integer titularId, String dniShopper,
      String numeroCheque, Long estadoId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    model.addAttribute("user", user);
    model.addAttribute("orderStates", orderRepository.findOrderStates());
    if (numeroOrden != null) {
      model.addAttribute("numeroOrden", numeroOrden);
    }
    model.addAttribute("tipoTitular", tipoTitular);
    if (titularId != null) {
      model.addAttribute("titularId", titularId);
      if (tipoTitular.equals(1)) {
        Shopper shopper = shopperRepository.get(titularId);
        model.addAttribute("titularNombre", shopper.getName());
      } else {
        Proveedor proveedor = proveedorRepository.get(titularId);
        model.addAttribute("titularNombre", proveedor.getDescription());
      }
    }
    if (numeroOrden != null) {
      model.addAttribute("numeroOrden", numeroOrden);
    }
    if (dniShopper != null && !dniShopper.isEmpty()) {
      model.addAttribute("dniShopper", dniShopper);
    }
    if (numeroCheque != null && !numeroCheque.isEmpty()) {
      model.addAttribute("numeroCheque", numeroCheque);
    }

    List<OrdenPago> ordenes = new ArrayList<OrdenPago>();
    if (numeroOrden != null) {
      OrdenPago ordenPago = orderRepository.get(numeroOrden);
      if (ordenPago != null) {
        ordenes.add(ordenPago);
      }
    } else if (titularId != null
        || (dniShopper != null && !dniShopper.isEmpty())
        || (numeroCheque != null && !numeroCheque.isEmpty())
        || estadoId != null) {
      OrderState estado = null;
      if (estadoId != null) {
        estado = orderRepository.getOrderState(estadoId);
        model.addAttribute("state", estadoId);
      }
      ordenes = orderRepository.findOrdenes(tipoTitular, titularId, dniShopper,
          numeroCheque, estado);
    }
    model.addAttribute("ordenes", ordenes);
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
    return "caratula";
  }
}