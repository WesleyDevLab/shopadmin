package com.ibiscus.shopnchek.application.order;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibiscus.shopnchek.application.Command;
import com.ibiscus.shopnchek.domain.admin.ItemOrden;
import com.ibiscus.shopnchek.domain.admin.OrdenPago;
import com.ibiscus.shopnchek.domain.admin.OrderRepository;
import com.ibiscus.shopnchek.domain.admin.Shopper;
import com.ibiscus.shopnchek.domain.admin.ShopperRepository;

public class GetOrderCommand implements Command<OrdenPago> {

	private OrderRepository orderRepository;

	private ShopperRepository shopperRepository;

	private long numero;

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public OrdenPago execute() {
		OrdenPago order = orderRepository.get(numero);
		for (ItemOrden itemOrden : order.getItems()) {
			Shopper itemShopper = shopperRepository.findByDni(itemOrden.getShopperDni());
			itemOrden.updateShopper(itemShopper);
		}

		return order;
	}

	public void setOrderRepository(final OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public void setShopperRepository(final ShopperRepository shopperRepository) {
		this.shopperRepository = shopperRepository;
	}

	public void setNumero(final long numero) {
		this.numero = numero;
	}

}
