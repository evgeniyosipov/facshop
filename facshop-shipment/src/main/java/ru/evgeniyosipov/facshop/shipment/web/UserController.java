package ru.evgeniyosipov.facshop.shipment.web;

import ru.evgeniyosipov.facshop.entity.Groups;
import ru.evgeniyosipov.facshop.entity.Person;
import ru.evgeniyosipov.facshop.shipment.qualifiers.LoggedIn;
import ru.evgeniyosipov.facshop.shipment.session.UserBean;
import ru.evgeniyosipov.facshop.shipment.web.util.JsfUtil;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Named(value = "userController")
@SessionScoped
public class UserController implements Serializable {

    private static final String BUNDLE = "bundles.Bundle";
    private static final long serialVersionUID = -7440104826420270291L;

    @Inject
    ShippingBean adapter;

    Person user;
    @EJB
    private UserBean ejbFacade;
    private String username;
    private String password;

    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String result;

        try {
            request.login(this.getUsername(), this.getPassword());

            this.user = ejbFacade.getUserByEmail(getUsername());
            this.getAuthenticatedUser();

            if (isAdmin(user)) {
                result = "/admin/index";
                JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "Login_Success"));

            } else {
                JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "OnlyAuthorizedPersonnel"));
                result = this.logout();
            }

        } catch (ServletException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);

            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Login_Failed"));
            result = "/login";
        }

        return result;
    }

    public boolean isAdmin(Person user) {
        for (Groups g : user.getGroupsList()) {
            if (g.getName().equals("ADMINS")) {
                return true;
            }
        }
        return false;
    }

    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            this.user = null;

            ((HttpSession) context.getExternalContext().getSession(false)).invalidate();
            request.logout();

            JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "Logout_Success"));

        } catch (ServletException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);

            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Logout_Failed"));
        }

        return "/index";
    }

    public UserBean getEjbFacade() {
        return ejbFacade;
    }

    public @Produces
    @LoggedIn
    Person getAuthenticatedUser() {
        return user;
    }

     public boolean isLogged() {
        if (FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal() != null) {
            user = ejbFacade.getUserByEmail(FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().toString());
            return isAdmin(user);
        } else {
            return false;
        }
    }

    public String getUsername() {
            return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Person getUser() {
        return user;
    }

}
