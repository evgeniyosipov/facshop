package ru.evgeniyosipov.facshop.store.ejb;

import ru.evgeniyosipov.facshop.entity.Customer;
import ru.evgeniyosipov.facshop.entity.Person;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import ru.evgeniyosipov.facshop.entity.Groups;

@Stateless
public class UserBean extends AbstractFacade<Customer> {

    @PersistenceContext(unitName = "facshopPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void create(Customer user) {
        Groups userGroup = (Groups) em.createNamedQuery("Groups.findByName")
                .setParameter("name", "USERS")
                .getSingleResult();
        user.getGroupsList().add(userGroup);
        userGroup.getPersonList().add(user);
        em.persist(user);
        em.merge(userGroup);
    }

    @Override
    public void remove(Customer user) {
        Groups userGroup = (Groups) em.createNamedQuery("Groups.findByName")
                .setParameter("name", "USERS")
                .getSingleResult();
        userGroup.getPersonList().remove(user);
        em.remove(em.merge(user));
        em.merge(userGroup);
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
