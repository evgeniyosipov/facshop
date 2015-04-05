package ru.evgeniyosipov.facshop.store.ejb;

import ru.evgeniyosipov.facshop.entity.Administrator;
import ru.evgeniyosipov.facshop.entity.Groups;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class AdministratorBean extends AbstractFacade<Administrator> {

    @PersistenceContext(unitName = "facshopPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AdministratorBean() {
        super(Administrator.class);
    }

    @Override
    public void create(Administrator admin) {
        Groups adminGroup = (Groups) em.createNamedQuery("Groups.findByName")
                .setParameter("name", "Administrator")
                .getSingleResult();
        admin.getGroupsList().add(adminGroup);
        adminGroup.getPersonList().add(admin);
        em.persist(admin);
        em.merge(adminGroup);
    }

    @Override
    public void remove(Administrator admin) {
        Groups adminGroup = (Groups) em.createNamedQuery("Groups.findByName")
                .setParameter("name", "Administrator")
                .getSingleResult();
        adminGroup.getPersonList().remove(admin);
        em.remove(em.merge(admin));
        em.merge(adminGroup);
    }

}
