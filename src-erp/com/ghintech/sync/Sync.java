/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ghintech.sync;

import com.openbravo.pos.erp.sync.DataLogicIntegration;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JRootApp;
import java.util.Properties;
import org.idempiere.webservice.client.base.LoginRequest;
import org.idempiere.webservice.client.net.WebServiceConnection;




/**
 *
 * @author egil
 */
public class Sync {
    protected DataLogicIntegration dlintegration;
    protected DataLogicSales dlsales;
    protected DataLogicSystem dlsystem;
    private String UrlBase;
    private int AD_Client_ID;
    private int AD_Org_ID;
    private int AD_Role_ID;
    private String UserName;
    private String UserPass;
    public Sync(JRootApp app) {
        dlintegration = (DataLogicIntegration) app.getBean("com.openbravo.pos.erp.sync.DataLogicIntegration");
        dlsales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlsystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        Properties idempiereProperties = dlsystem.getResourceAsProperties("idempiere.properties");
        setUrlBase(idempiereProperties.getProperty("UrlBase"));
        setAD_Client_ID(idempiereProperties.getProperty("AD_Client_ID"));
        setAD_Org_ID(idempiereProperties.getProperty("AD_Org_ID"));
        setAD_Role_ID(idempiereProperties.getProperty("AD_Role_ID"));
        setUserName(idempiereProperties.getProperty("UserName"));
        setUserPass(idempiereProperties.getProperty("UserPass"));
    }

    public String getUrlBase() {
        return UrlBase;
    }

    public final void setUrlBase(String UrlBase) {
        this.UrlBase = UrlBase;
    }
    
    public int getAD_Client_ID() {
        return AD_Client_ID;
    }

    public final void setAD_Client_ID(String AD_Client_ID) {
        this.AD_Client_ID = Integer.valueOf(AD_Client_ID);
    }

    public int getAD_Org_ID() {
        return AD_Org_ID;
    }

    public final void setAD_Org_ID(String AD_Org_ID) {
        this.AD_Org_ID = Integer.valueOf(AD_Org_ID);
    }

    public int getAD_Role_ID() {
        return AD_Role_ID;
    }

    public final void setAD_Role_ID(String AD_Role_ID) {
        this.AD_Role_ID = Integer.valueOf(AD_Role_ID);
    }

    public String getUserName() {
        return UserName;
    }

    public final void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getUserPass() {
        return UserPass;
    }

    public final void setUserPass(String UserPass) {
        this.UserPass = UserPass;
    }
    
    public LoginRequest getLogin() {
        LoginRequest login = new LoginRequest();
        login.setUser(getUserName());
        login.setPass(getUserPass());
        login.setClientID(getAD_Client_ID());
        login.setRoleID(getAD_Role_ID());
        login.setOrgID(getAD_Org_ID());
        return login;
    }
    
    public WebServiceConnection getClient() {
        WebServiceConnection client = new WebServiceConnection();
        client.setAttempts(3);
        client.setTimeout(2000);
        client.setAttemptsTimeout(2000);
        client.setUrl(getUrlBase());
        client.setAppName("Unicenta Integration");
        return client;
    }

    
    
    
}

