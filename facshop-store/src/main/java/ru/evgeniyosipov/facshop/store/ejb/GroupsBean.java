package ru.evgeniyosipov.facshop.store.ejb;

import ru.evgeniyosipov.facshop.entity.Groups;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class GroupsBean extends AbstractFacade<Groups> {

    @PersistenceContext(unitName = "facshopPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public GroupsBean() {
        super(Groups.class);
    }

}
