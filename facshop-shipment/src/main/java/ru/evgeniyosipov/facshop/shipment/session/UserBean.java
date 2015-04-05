package ru.evgeniyosipov.facshop.shipment.session;

import ru.evgeniyosipov.facshop.entity.Customer;
import ru.evgeniyosipov.facshop.entity.Person;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class UserBean extends AbstractFacade<Customer> {

    @PersistenceContext(unitName = "facshopPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public Person getUserByEmail(String email) {
        Query createNamedQuery = getEntityManager().createNamedQuery("Person.findByEmail");

        createNamedQuery.setParameter("email", email);

        return (Person) createNamedQuery.getSingleResult();
    }

    public UserBean() {
        super(Customer.class);
    }

}
