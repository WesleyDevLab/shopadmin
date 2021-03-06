package com.ibiscus.shopnchek.application.debt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.poi.ss.usermodel.Workbook;

import com.ibiscus.shopnchek.application.Command;
import com.ibiscus.shopnchek.application.util.ExcelWriter;
import com.ibiscus.shopnchek.domain.debt.Debt;
import com.ibiscus.shopnchek.domain.debt.TipoItem;
import com.ibiscus.shopnchek.domain.debt.TipoPago;
import com.ibiscus.shopnchek.domain.debt.Debt.State;
import com.ibiscus.shopnchek.domain.debt.DebtRepository;

public class ExportDebtCommand implements Command<Workbook> {

	private final DebtRepository debtRepository;

	private String shopperDni;

	private Date from;

	private Date to;

	private String tipoPago;

	private String tipoItem;

	public ExportDebtCommand(final DebtRepository debtRepository) {
		Validate.notNull(debtRepository, "The debt repository cannot be null");
		this.debtRepository = debtRepository;
	}

	public void setShopperDni(final String shopperDni) {
		this.shopperDni = shopperDni;
	}

	public void setFrom(final Date from) {
		this.from = from;
	}

	public void setTo(final Date to) {
		this.to = to;
	}

	public void setTipoPago(final String tipoPago) {
		this.tipoPago = tipoPago;
	}

	public void setTipoItem(final String tipoItem) {
		this.tipoItem = tipoItem;
	}

	private TipoPago getTipoPago() {
		TipoPago tipoPagoDeuda = null;
		if (!StringUtils.isBlank(tipoPago)) {
			tipoPagoDeuda = TipoPago.valueOf(tipoPago);
		}
		return tipoPagoDeuda;
	}

	private TipoItem getTipoItem() {
		TipoItem tipoItemDeuda = null;
		if (!StringUtils.isBlank(tipoItem)) {
			tipoItemDeuda = TipoItem.valueOf(tipoItem);
		}
		return tipoItemDeuda;
	}

	@Override
	public Workbook execute() {
		if (from != null && to != null) {
			Validate.isTrue(from.before(to), "The FROM date must be before the TO date");
		}

		List<Debt> items = debtRepository.find(shopperDni, State.pendiente, from,
				to, getTipoPago(), getTipoItem());
		List<String> header = new ArrayList<String>();
		header.add("Shopper");
		header.add("Empresa");
		header.add("Local");
		header.add("Importe");
		header.add("Fecha");
		ExcelWriter writer = new ExcelWriter(header);
		for (Debt debt : items) {
			List<Object> columns = new ArrayList<Object>();
			columns.add(debt.getShopperDni());
			if (debt.getClient() != null) {
				columns.add(debt.getClient().getName());
			} else {
				columns.add(debt.getClientDescription());
			}
			if (debt.getBranch() != null) {
				columns.add(debt.getBranch().getAddress());
			} else {
				columns.add(debt.getBranchDescription());
			}
			columns.add(new Double(debt.getImporte()));
			columns.add(debt.getFecha());
			writer.write(columns);
		}
		return writer.getWorkbook();
	}
}
