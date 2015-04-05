package ru.evgeniyosipov.facshop.store.ejb;

import ru.evgeniyosipov.facshop.entity.OrderStatus;
import java.io.Serializable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class OrderStatusBean extends AbstractFacade<OrderStatus> implements Serializable {

    private static final long serialVersionUID = 5199196331433553237L;
    @PersistenceContext(unitName = "facshopPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public OrderStatusBean() {
        super(OrderStatus.class);
    }

    public OrderStatus getStatusByName(String status) {
        Query createNamedQuery = getEntityManager().createNamedQuery("OrderStatus.findByStatus");
        createNamedQuery.setParameter("status", status);
        return (OrderStatus) createNamedQuery.getSingleResult();
    }

}
