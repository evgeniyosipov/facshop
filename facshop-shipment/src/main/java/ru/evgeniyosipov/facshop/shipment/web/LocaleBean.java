package ru.evgeniyosipov.facshop.shipment.web;

import java.io.Serializable;
import java.util.Locale;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named(value = "localeBean")
@SessionScoped
public class LocaleBean implements Serializable {

    private static final long serialVersionUID = -5322261872682904868L;

    public LocaleBean() {
    }

    private Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return locale.getLanguage();
    }

    public void setLanguage(String language) {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

}
