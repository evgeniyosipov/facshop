package ru.evgeniyosipov.facshop.store.web;

import ru.evgeniyosipov.facshop.store.ejb.OrderServiceBean;
import ru.evgeniyosipov.facshop.store.ejb.OrderJMSManager;
import ru.evgeniyosipov.facshop.entity.CustomerOrder;
import ru.evgeniyosipov.facshop.entity.Person;
import ru.evgeniyosipov.facshop.store.qualifiers.LoggedIn;
import ru.evgeniyosipov.facshop.store.web.util.AbstractPaginationHelper;
import ru.evgeniyosipov.facshop.store.web.util.JsfUtil;
import ru.evgeniyosipov.facshop.store.web.util.PageNavigation;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

@Named(value = "customerOrderController")
@SessionScoped
public class CustomerOrderController implements Serializable {

    private static final String BUNDLE = "bundles.Bundle";
    private static final long serialVersionUID = 8606060319870740714L;
    @Inject
    @LoggedIn
    private Person user;
    private List<CustomerOrder> myOrders;
    private CustomerOrder current;
    private DataModel items = null;
    @EJB
    private ru.evgeniyosipov.facshop.store.ejb.OrderServiceBean ejbFacade;
    @EJB
    private OrderJMSManager orderJMSManager;
    private AbstractPaginationHelper pagination;
    private int selectedItemIndex;
    private String searchString;
    private static final Logger logger = Logger.getLogger(CustomerOrderController.class.getCanonicalName());

    public CustomerOrderController() {
    }

    public CustomerOrder getSelected() {
        if (current == null) {
            current = new CustomerOrder();
            selectedItemIndex = -1;
        }
        return current;
    }

    private OrderServiceBean getFacade() {
        return ejbFacade;
    }

    public AbstractPaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new AbstractPaginationHelper(10) {
                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public PageNavigation prepareList() {
        recreateModel();
        return PageNavigation.LIST;
    }

    public PageNavigation prepareView() {
        current = (CustomerOrder) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return PageNavigation.VIEW;
    }

    public PageNavigation prepareCreate() {
        current = new CustomerOrder();
        selectedItemIndex = -1;
        return PageNavigation.CREATE;
    }

    public PageNavigation create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CustomerOrderCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public PageNavigation prepareEdit() {
        current = (CustomerOrder) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return PageNavigation.EDIT;
    }

    public PageNavigation update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CustomerOrderUpdated"));
            return PageNavigation.VIEW;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public PageNavigation destroy() {
        current = (CustomerOrder) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return PageNavigation.LIST;
    }

    public PageNavigation cancelOrder() {
        current = (CustomerOrder) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();

        try {
            orderJMSManager.deleteMessage(current.getId());

            ejbFacade.setOrderStatus(current.getId(), String.valueOf(OrderServiceBean.Status.CANCELLED_MANUAL.getStatus()));

            recreateModel();
            return PageNavigation.LIST;
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return PageNavigation.INDEX;
    }

    public List<CustomerOrder> getMyOrders() {

        if (user != null) {

            myOrders = getFacade().getOrderByCustomerId(user.getId());
            if (myOrders.isEmpty()) {

                logger.log(Level.FINEST, "Customer {0} has no orders to display.", user.getEmail());
                return null;
            } else {
                logger.log(Level.FINEST, "Order amount:{0}", myOrders.get(0).getAmount());
                return myOrders;
            }

        } else {

            JsfUtil.addErrorMessage("Current user is not authenticated. Please do login before accessing your orders.");

            return null;
        }
    }

    public PageNavigation destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return PageNavigation.VIEW;
        } else {
            recreateModel();
            return PageNavigation.LIST;
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);

            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CustomerOrderDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            selectedItemIndex = count - 1;
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    public PageNavigation next() {
        getPagination().nextPage();
        recreateModel();
        return PageNavigation.LIST;
    }

    public PageNavigation previous() {
        getPagination().previousPage();
        recreateModel();
        return PageNavigation.LIST;
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    @FacesConverter(forClass = CustomerOrder.class)
    public static class CustomerOrderControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CustomerOrderController controller = (CustomerOrderController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "customerOrderController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof CustomerOrder) {
                CustomerOrder o = (CustomerOrder) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + CustomerOrderController.class.getName());
            }
        }
    }

}
