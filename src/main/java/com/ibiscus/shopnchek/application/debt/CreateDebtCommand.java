package com.ibiscus.shopnchek.application.debt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibiscus.shopnchek.application.Command;
import com.ibiscus.shopnchek.domain.admin.Shopper;
import com.ibiscus.shopnchek.domain.admin.ShopperRepository;
import com.ibiscus.shopnchek.domain.debt.Branch;
import com.ibiscus.shopnchek.domain.debt.BranchRepository;
import com.ibiscus.shopnchek.domain.debt.Client;
import com.ibiscus.shopnchek.domain.debt.ClientRepository;
import com.ibiscus.shopnchek.domain.debt.Debt;
import com.ibiscus.shopnchek.domain.debt.DebtRepository;
import com.ibiscus.shopnchek.domain.debt.TipoItem;
import com.ibiscus.shopnchek.domain.debt.TipoPago;
import com.ibiscus.shopnchek.domain.security.User;

public class CreateDebtCommand implements Command<List<Debt>> {

	private DebtRepository debtRepository;

	private BranchRepository branchRepository;

	private ClientRepository clientRepository;

	private ShopperRepository shopperRepository;

	private VisitaDto visitaDto;

	private User operator;

	public CreateDebtCommand() {
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<Debt> execute() {
		Validate.notNull(visitaDto, "The visit DTO cannot be null");
		List<Debt> debts = new ArrayList<Debt>();

		TipoItem tipoItem = TipoItem.manual;
		Client client = null;
		if (visitaDto.getClientId() != null) {
			client = clientRepository.get(visitaDto.getClientId());
		}
		Branch branch = null;
		if (visitaDto.getBranchId() != null) {
			branch = branchRepository.get(visitaDto.getBranchId());
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date fecha;
		try {
			fecha = dateFormat.parse(visitaDto.getFecha());
		} catch (ParseException e) {
			throw new RuntimeException("Cannot parse visit date", e);
		}
		for (DebtDto debtDto : visitaDto.getItems()) {
			TipoPago tipoPago = TipoPago.valueOf(debtDto.getTipoPago());
			Debt debt = new Debt(tipoItem, tipoPago, visitaDto.getShopperDni(),
					debtDto.getImporte(), fecha, debtDto.getObservacion(),
					null, client, visitaDto.getClientDescription(), branch,
					visitaDto.getBranchDescription(),
					null, operator.getUsername());
			debtRepository.save(debt);
			Shopper shopper = shopperRepository.findByDni(debt.getShopperDni());
			debt.updateShopper(shopper);
			debts.add(debt);
		}
		return debts;
	}

	public void setDebtRepository(final DebtRepository debtRepository) {
		this.debtRepository = debtRepository;
	}

	public void setClientRepository(final ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	public void setBranchRepository(final BranchRepository branchRepository) {
		this.branchRepository = branchRepository;
	}

	public void setShopperRepository(final ShopperRepository shopperRepository) {
		this.shopperRepository = shopperRepository;
	}

	public void setVisitaDto(final VisitaDto visitaDto) {
		this.visitaDto = visitaDto;
	}

	public void setOperator(final User operator) {
		this.operator = operator;
	}
}
