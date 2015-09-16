/**
 * ExternalSalesImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.openbravo.pos.erp.externalsales;

public interface ExternalSalesImpl extends java.rmi.Remote {
    public com.openbravo.pos.erp.externalsales.Product[] getProductsCatalog(java.lang.String clientID, java.lang.String organizationId, java.lang.String salesChannel, java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
    public com.openbravo.pos.erp.externalsales.ProductPlus[] getProductsPlusCatalog(java.lang.String clientID, java.lang.String organizationId, java.lang.String salesChannel, java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
    public boolean uploadOrders(java.lang.String clientID, java.lang.String organizationId, java.lang.String salesChannel, com.openbravo.pos.erp.externalsales.Order[] newOrders, java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
    public com.openbravo.pos.erp.externalsales.Order[] getOrders(java.lang.String clientID, java.lang.String organizationId, com.openbravo.pos.erp.externalsales.OrderIdentifier[] orderIds, java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
}
