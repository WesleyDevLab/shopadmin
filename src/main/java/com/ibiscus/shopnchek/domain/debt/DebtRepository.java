package com.ibiscus.shopnchek.domain.debt;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.ibiscus.shopnchek.application.debt.AbstractGetPendingDebtCommand;
import com.ibiscus.shopnchek.domain.debt.Debt.State;
import com.ibiscus.shopnchek.domain.util.Row;

public class DebtRepository extends HibernateDaoSupport {

	private static Logger logger = Logger.getLogger(DebtRepository.class.getName());

	public Debt get(final long id) {
		return getHibernateTemplate().get(Debt.class, id);
	}

	public Debt getByExternalId(final long externalId, final TipoItem tipoItem,
			final TipoPago tipoPago) {
		Criteria criteria = getSession().createCriteria(Debt.class);
		criteria.add(Expression.eq("externalId", externalId));
		criteria.add(Expression.eq("tipoItem", tipoItem));
		criteria.add(Expression.eq("tipoPago", tipoPago));
		return (Debt) criteria.uniqueResult();
	}

	public Long save(final Debt debt) {
		return (Long) getHibernateTemplate().save(debt);
	}

	public void update(final Debt debt) {
		getHibernateTemplate().update(debt);
	}

	public Criteria getCriteria(final String shopperDni, final State state,
			final Date from, final Date to, final TipoPago tipoPago, final TipoItem tipoItem,
			final String owner) {
		Criteria criteria = getSession().createCriteria(Debt.class);
		if (!StringUtils.isBlank(shopperDni)) {
			criteria.add(Expression.eq("shopperDni", shopperDni));
		}
		if (state != null) {
			criteria.add(Expression.eq("estado", state));
		}
		if (from != null) {
			criteria.add(Expression.ge("fecha", from));
		}
		if (to != null) {
			criteria.add(Expression.le("fecha", to));
		}
		if (tipoPago != null) {
			criteria.add(Expression.eq("tipoPago", tipoPago));
		}
		if (tipoItem != null) {
			criteria.add(Expression.eq("tipoItem", tipoItem));
		}
		if (owner != null) {
			criteria.add(Expression.eq("usuario", owner));
		}
		return criteria;
	}

	public List<Debt> find(final String shopperDni, final State state,
			final Date from, final Date to, final TipoPago tipoPago, final TipoItem tipoItem) {
		return find(null, null, null, true, shopperDni, state, from, to, tipoPago,
				tipoItem, null);
	}

	@SuppressWarnings("unchecked")
	public List<Row> getSummary(final String shopperDni, final List<State> states,
			final Date from, final Date to, List<String> groupBy) {
		/*Criteria criteria = getCriteria(shopperDni, from, to);
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.sqlGroupProjection("YEAR(fecha) as year, MONTH(fecha) as month",
				"YEAR(fecha), MONTH(fecha)", new String[] {"year", "month"},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER }));
		for (String propertyName : groupBy) {
			projectionList.add(Projections.groupProperty(propertyName));
		}
		criteria.setProjection(projectionList);
		//criteria.addOrder(Order.asc("year"));
		//criteria.addOrder(Order.asc("month"));
		for (String propertyName : groupBy) {
			criteria.addOrder(Order.asc(propertyName));
		}
		criteria.setResultTransformer(new RowResultTransformer());*/
		StringBuilder builder = new StringBuilder("select year(fecha) as year, month(fecha) as month, ");
		for (String columnName : groupBy) {
			builder.append(columnName);
			builder.append(", ");
		}
		builder.append("sum(case when deuda.tipo_pago = 'honorarios' then deuda.importe else 0 end) as honorarios, ");
		builder.append("sum(case when deuda.tipo_pago = 'reintegros' then deuda.importe else 0 end) as reintegros, ");
		builder.append("sum(case when deuda.tipo_pago = 'otrosgastos' then deuda.importe else 0 end) as otros ");
		builder.append("from deuda ");
		builder.append("left join clients on (deuda.client_id = clients.id) ");
		builder.append("where deuda.fecha >= :from and deuda.fecha <= :to ");
		if (states != null && !states.isEmpty()) {
			builder.append("and (");
			for (int i = 0; i < states.size(); i++) {
				if (i == 0) {
					builder.append("deuda.estado = :state" + i + " ");
				} else {
					builder.append("OR deuda.estado = :state" + i + " ");
				}
			}
			builder.append(") ");
		}
		builder.append("group by year(fecha), month(fecha) ");
		for (String columnName : groupBy) {
			builder.append(", ");
			builder.append(columnName);
			builder.append(" ");
		}
		builder.append("order by year(fecha), month(fecha)");
		for (String columnName : groupBy) {
			builder.append(", ");
			builder.append(columnName);
		}
		SQLQuery query = getSession().createSQLQuery(builder.toString());
		query.setDate("from", from);
		query.setDate("to", to);
		if (states != null && !states.isEmpty()) {
			for (int i = 0; i < states.size(); i++) {
				query.setString("state" + i, states.get(i).toString());
			}
		}
		query.setResultTransformer(new RowResultTransformer());
		return query.list();
	}

	@SuppressWarnings("unchecked")
	public List<Debt> find(final Integer start, final Integer count,
			final String orderBy, final boolean asc, final String shopperDni,
			final State state, final Date from, final Date to, final TipoPago tipoPago,
			final TipoItem tipoItem, final String owner) {
		Criteria criteria = getCriteria(shopperDni, state, from, to, tipoPago,
				tipoItem, owner);
		if (start != null) {
			criteria.setFirstResult(start);
		}
		if (count != null) {
			criteria.setMaxResults(count);
		}
		if (orderBy != null) {
			criteria.createAlias("client", "c");
			if (asc) {
				criteria.addOrder(Order.asc(orderBy));
			} else {
				criteria.addOrder(Order.desc(orderBy));
			}
			if (!orderBy.equals("fecha")) {
				criteria.addOrder(Order.asc("fecha"));
			}
		}
		logger.info("Debt query: " + criteria);
		return criteria.list();
	}

	public Integer getCount(final String shopperDni, final State state, final Date from,
			final Date to, final TipoPago tipoPago, final TipoItem tipoItem, final String owner) {
		Criteria criteria = getCriteria(shopperDni, state, from, to, tipoPago, tipoItem, owner);
		return (Integer) criteria.setProjection(Projections.rowCount()).uniqueResult();
	}
}
