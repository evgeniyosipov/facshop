package ru.evgeniyosipov.facshop.store.ejb;

import ru.evgeniyosipov.facshop.entity.Category;
import ru.evgeniyosipov.facshop.entity.Product;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Stateless
public class ProductBean extends AbstractFacade<Product> {

    private static final Logger logger
            = Logger.getLogger(ProductBean.class.getCanonicalName());

    @PersistenceContext(unitName = "facshopPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProductBean() {
        super(Product.class);
    }

    public List<Product> findByCategory(int[] range, int categoryId) {
        Category cat = new Category();
        cat.setId(categoryId);

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Product> query = qb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);
        query.where(qb.equal(product.get("category"), cat));

        List<Product> result = this.findRange(range, query);

        logger.log(Level.FINEST, "Product List size: {0}", result.size());

        return result;
    }

}
