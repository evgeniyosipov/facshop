package ru.evgeniyosipov.facshop.store.ejb;

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

    public boolean createUser(Customer customer) {

        if (getUserByEmail(customer.getEmail()) == null) {
            super.create(customer);
            return true;
        } else {
            return false;
        }
    }

    public Person getUserByEmail(String email) {
        Query createNamedQuery = getEntityManager().createNamedQuery("Person.findByEmail");

        createNamedQuery.setParameter("email", email);

        if (createNamedQuery.getResultList().size() > 0) {
            return (Person) createNamedQuery.getSingleResult();
        } else {
            return null;
        }
    }

    public UserBean() {
        super(Customer.class);
    }

}
