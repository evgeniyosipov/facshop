package ru.evgeniyosipov.facshop.store.ejb;

import ru.evgeniyosipov.facshop.entity.CustomerOrder;
import ru.evgeniyosipov.facshop.entity.OrderStatus;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.*;

@Stateless
@Path("/orders")
public class OrderServiceBean extends AbstractFacade<CustomerOrder> implements Serializable {

    private static final Logger logger
            = Logger.getLogger(ShoppingCart.class.getCanonicalName());
    private static final long serialVersionUID = -2407971550575800416L;
    @PersistenceContext(unitName = "facshopPU")
    private EntityManager em;
    CustomerOrder order;
    @EJB
    OrderStatusBean statusBean;

    public OrderServiceBean() {
        super(CustomerOrder.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<CustomerOrder> getOrderByCustomerId(Integer id) {
        Query createNamedQuery = getEntityManager().createNamedQuery("CustomerOrder.findByCustomerId");

        createNamedQuery.setParameter("id", id);

        return createNamedQuery.getResultList();
    }

    public CustomerOrder getOrderById(Integer id) {
        Query createNamedQuery = getEntityManager().createNamedQuery("CustomerOrder.findById");
        createNamedQuery.setParameter("id", id);
        return (CustomerOrder) createNamedQuery.getSingleResult();
    }

    @GET
    @Produces({"application/xml", "application/json"})
    public List<CustomerOrder> getOrderByStatus(@QueryParam("status") int status) {

        Query createNamedQuery = getEntityManager().createNamedQuery("CustomerOrder.findByStatus");

        OrderStatus result = statusBean.find(status);

        createNamedQuery.setParameter("status", result.getStatus());
        List<CustomerOrder> orders = createNamedQuery.getResultList();

        return orders;
    }

    @PUT
    @Path("{orderId}")
    @Produces({"application/xml", "application/json"})
    public void setOrderStatus(@PathParam("orderId") int orderId, String newStatus) {

        logger.log(Level.INFO, "Order id:{0} - Status:{1}", new Object[]{orderId, newStatus});

        try {
            order = this.find(orderId);

            if (order != null) {
                logger.log(Level.FINEST, "Updating order {0} status to {1}", new Object[]{order.getId(), newStatus});

                OrderStatus oStatus = statusBean.find(new Integer(newStatus));
                order.setOrderStatus(oStatus);

                em.merge(order);

                logger.info("Order Updated!");
            }

        } catch (Exception ex) {

            logger.log(Level.SEVERE, ex.getMessage());
        }

    }

    public enum Status {

        PENDING_PAYMENT(2),
        READY_TO_SHIP(3),
        SHIPPED(4),
        CANCELLED_PAYMENT(5),
        CANCELLED_MANUAL(6);
        private int status;

        private Status(int pStatus) {
            status = pStatus;
        }

        public int getStatus() {
            return status;
        }
    }

}
