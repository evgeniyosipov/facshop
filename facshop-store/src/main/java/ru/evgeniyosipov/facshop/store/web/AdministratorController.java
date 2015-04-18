package ru.evgeniyosipov.facshop.store.web;

import ru.evgeniyosipov.facshop.store.ejb.AdministratorBean;
import ru.evgeniyosipov.facshop.entity.Administrator;
import ru.evgeniyosipov.facshop.store.web.util.AbstractPaginationHelper;
import ru.evgeniyosipov.facshop.store.web.util.JsfUtil;
import ru.evgeniyosipov.facshop.store.web.util.PageNavigation;
import java.io.Serializable;
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
import javax.inject.Named;
import ru.evgeniyosipov.facshop.entity.Person;
import ru.evgeniyosipov.facshop.store.web.util.MD5Util;

@Named(value = "administratorController")
@SessionScoped
public class AdministratorController implements Serializable {

    private static final String BUNDLE = "bundles.Bundle";
    private static final long serialVersionUID = -2691147357609941284L;

    private Administrator current;
    private DataModel items = null;
    @EJB
    private ru.evgeniyosipov.facshop.store.ejb.AdministratorBean ejbFacade;

    private static final Logger logger = Logger.getLogger(CustomerController.class.getCanonicalName());

    private AbstractPaginationHelper pagination;
    private int selectedItemIndex;

    public AdministratorController() {
    }

    public Administrator getSelected() {
        if (current == null) {
            current = new Administrator();
            selectedItemIndex = -1;
        }
        return current;
    }

    private AdministratorBean getFacade() {
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
        current = (Administrator) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return PageNavigation.VIEW;
    }

    private boolean isAdministratorDuplicated(Person p) {
        return (getFacade().getAdministratorByEmail(p.getEmail()) != null);
    }

    public PageNavigation prepareCreate() {
        current = new Administrator();
        selectedItemIndex = -1;
        return PageNavigation.CREATE;
    }

    public PageNavigation create() {
        try {
            if (!isAdministratorDuplicated(current)) {
                current.setPassword(MD5Util.generateMD5(current.getPassword()));
                getFacade().create(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("AdministratorCreated"));
                return PageNavigation.VIEW;
            } else {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("AdministratorDuplicatedError"));

            }
            return PageNavigation.VIEW;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public PageNavigation prepareEdit() {
        current = (Administrator) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return PageNavigation.EDIT;
    }

    public PageNavigation update() {
        try {
            logger.log(Level.INFO, "Updating administrator ID:{0}", current.getId());
            current.setPassword(MD5Util.generateMD5(current.getPassword()));
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("AdministratorUpdated"));
            return PageNavigation.VIEW;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public PageNavigation destroy() {
        current = (Administrator) getItems().getRowData();
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
            if (getFacade().isLastAdmimistrator()) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("AdministratorLastNonDelete"));
            } else {
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("AdministratorDeleted"));
            }
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

    @FacesConverter(forClass = Administrator.class)
    public static class AdministratorControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AdministratorController controller = (AdministratorController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "administratorController");
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
            if (object instanceof Administrator) {
                Administrator o = (Administrator) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + AdministratorController.class.getName());
            }
        }
    }

}
