package ru.evgeniyosipov.facshop.store.web;

import ru.evgeniyosipov.facshop.store.ejb.OrderDetailBean;
import ru.evgeniyosipov.facshop.entity.OrderDetail;
import ru.evgeniyosipov.facshop.store.web.util.AbstractPaginationHelper;
import ru.evgeniyosipov.facshop.store.web.util.JsfUtil;
import ru.evgeniyosipov.facshop.store.web.util.PageNavigation;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Named;

@Named(value = "orderDetailController")
@RequestScoped
public class OrderDetailController {

    private static final String BUNDLE = "bundles.Bundle";

    private OrderDetail current;
    private DataModel items = null;
    @EJB
    private ru.evgeniyosipov.facshop.store.ejb.OrderDetailBean ejbFacade;
    private AbstractPaginationHelper pagination;
    private int selectedItemIndex;

    public OrderDetailController() {
    }

    public OrderDetail getSelected() {
        if (current == null) {
            current = new OrderDetail();
            selectedItemIndex = -1;
        }
        return current;
    }

    private OrderDetailBean getFacade() {
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
                    int orderId = Integer.valueOf(JsfUtil.getRequestParameter("orderId"));
                    return new ListDataModel(getFacade().findOrderDetailByOrder(orderId));
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
        current = (OrderDetail) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return PageNavigation.VIEW;
    }

    public PageNavigation prepareCreate() {
        current = new OrderDetail();
        selectedItemIndex = -1;
        return PageNavigation.CREATE;
    }

    public PageNavigation create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("OrderDetailCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public PageNavigation prepareEdit() {
        current = (OrderDetail) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return PageNavigation.EDIT;
    }

    public PageNavigation update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("OrderDetailUpdated"));
            return PageNavigation.VIEW;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public PageNavigation destroy() {
        current = (OrderDetail) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return PageNavigation.LIST;
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("OrderDetailDeleted"));
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

    @FacesConverter(forClass = OrderDetail.class)
    public static class OrderDetailControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            OrderDetailController controller = (OrderDetailController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "orderDetailController");
            return controller.ejbFacade.find(getKey(value));
        }

        ru.evgeniyosipov.facshop.entity.OrderDetailPK getKey(String value) {
            ru.evgeniyosipov.facshop.entity.OrderDetailPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new ru.evgeniyosipov.facshop.entity.OrderDetailPK();
            key.setOrderId(Integer.parseInt(values[0]));
            key.setProductId(Integer.parseInt(values[1]));
            return key;
        }

        String getStringKey(ru.evgeniyosipov.facshop.entity.OrderDetailPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getOrderId());
            sb.append(SEPARATOR);
            sb.append(value.getProductId());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof OrderDetail) {
                OrderDetail o = (OrderDetail) object;
                return getStringKey(o.getOrderDetailPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + OrderDetailController.class.getName());
            }
        }
    }

}
