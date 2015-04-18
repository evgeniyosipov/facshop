package ru.evgeniyosipov.facshop.store.ejb;

import ru.evgeniyosipov.facshop.entity.Administrator;
import ru.evgeniyosipov.facshop.entity.Groups;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import ru.evgeniyosipov.facshop.entity.Person;

@Stateless
public class AdministratorBean extends AbstractFacade<Administrator> {

    @PersistenceContext(unitName = "facshopPU")
    private EntityManager em;

    private boolean lastAdministrator;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public Person getAdministratorByEmail(String email) {
        Query createNamedQuery = getEntityManager().createNamedQuery("Person.findByEmail");

        createNamedQuery.setParameter("email", email);

        if (createNamedQuery.getResultList().size() > 0) {
            return (Person) createNamedQuery.getSingleResult();
        } else {
            return null;
        }
    }

    public AdministratorBean() {
        super(Administrator.class);
    }

    @Override
    public void create(Administrator admin) {
        Groups adminGroup = (Groups) em.createNamedQuery("Groups.findByName")
                .setParameter("name", "ADMINS")
                .getSingleResult();
        admin.getGroupsList().add(adminGroup);
        adminGroup.getPersonList().add(admin);
        em.persist(admin);
        em.merge(adminGroup);
    }

    public boolean isLastAdmimistrator() {
        return lastAdministrator;
    }

    @Override
    public void remove(Administrator admin) {
        Groups adminGroup = (Groups) em.createNamedQuery("Groups.findByName")
                .setParameter("name", "ADMINS")
                .getSingleResult();
        if (adminGroup.getPersonList().size() > 1) {
            adminGroup.getPersonList().remove(admin);
            em.remove(em.merge(admin));
            em.merge(adminGroup);
            lastAdministrator = false;
        } else {
            lastAdministrator = true;
        }
    }

}
