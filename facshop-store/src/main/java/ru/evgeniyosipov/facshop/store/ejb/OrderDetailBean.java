package ru.evgeniyosipov.facshop.store.ejb;

import ru.evgeniyosipov.facshop.entity.OrderDetail;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class OrderDetailBean extends AbstractFacade<OrderDetail> {

    @PersistenceContext(unitName = "facshopPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public OrderDetailBean() {
        super(OrderDetail.class);
    }

    public List<OrderDetail> findOrderDetailByOrder(int orderId) {
        List<OrderDetail> details = getEntityManager().createNamedQuery("OrderDetail.findByOrderId").setParameter("orderId", orderId).getResultList();

        return details;
    }

}
