//    Openbravo POS is a point of sales application designed for touch screens.
//    http://www.openbravo.com/product/pos
//    Copyright (c) 2007 openTrends Solucions i Sistemes, S.L
//    Modified by Openbravo SL on March 22, 2007
//    These modifications are copyright Openbravo SL
//    Author/s: A. Romero
//    You may contact Openbravo SL at: http://www.openbravo.com
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.erp.sync;

import java.util.List;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataParams;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.SerializerWriteParams;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.Transaction;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.erp.customers.User;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.payment.PaymentInfoTicket;
import com.openbravo.pos.ticket.CategoryInfo;
import com.openbravo.pos.erp.externalsales.ClosedCashInfo;
import com.openbravo.pos.erp.externalsales.CreditNoteInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.inventory.StockCurrentInfo;

/**
 *
 * @author adrianromero
 * Created on 5 de marzo de 2007, 19:56
 * @contributor Sergio Oropeza - Double Click Sistemas - Venezuela - soropeza@dcs.net.ve, info@dcs.net.ve
 *
 */
public class DataLogicIntegration extends BeanFactoryDataSingle {
    
    protected Session s;

    /** Creates a new instance of DataLogicIntegration */
    public DataLogicIntegration() {
    }
    
    public void init(Session s) {
        this.s = s;
    }
     
    public void syncCustomersBefore() throws BasicException {
        new StaticSentence(s, "UPDATE CUSTOMERS SET VISIBLE = " + s.DB.FALSE()).exec();
    }

    public void syncCustomer(final CustomerInfoExt customer) throws BasicException {

        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {
                // Sync the Customer in a transaction

                // Try to update
	        // Add the field TaxID to sync...
                if (new PreparedSentence(s,
                            "UPDATE CUSTOMERS SET TAXID = ?, NAME = ?, ADDRESS = ?, VISIBLE = ?, MAXDEBT=? WHERE SEARCHKEY = ? OR ID=? ",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, customer.getTaxid());
                                setString(2, customer.getName());
                                setString(3, customer.getAddress());
                                setBoolean(4, customer.isVisible());
                                setDouble(5, customer.getMaxdebt());
                                setString(6, customer.getSearchkey());
                                setString(7, customer.getId());
                            }}) == 0) {

                    // If not updated, try to insert
                    new PreparedSentence(s,
                            "INSERT INTO CUSTOMERS(ID, TAXID, SEARCHKEY, NAME, NOTES, VISIBLE, MAXDEBT) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, customer.getId());
                                setString(2, customer.getTaxid());
                                setString(3, customer.getSearchkey());
                                setString(4, customer.getName());
                                setString(5, customer.getAddress());
                                setBoolean(6, customer.isVisible());
                                setDouble(7, customer.getMaxdebt());
                            }});
                }

                return null;
            }
        };
        t.execute();
    }
        
    public void syncPeople(final User user) throws BasicException {

        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {
                // Sync the Customer in a transaction

                // Try to update
	        // Add the field TaxID to sync...
                if (new PreparedSentence(s,
                            "UPDATE PEOPLE SET NAME = ?, VISIBLE = ? WHERE ID = ? OR NAME = ?",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, user.getName());
                                setBoolean(2, user.getVisible());
                                setString(3, user.getId());
                                setString(4, user.getName());
                            }}) == 0) {

                    // If not updated, try to insert
                    new PreparedSentence(s,
                            "INSERT INTO PEOPLE(ID, NAME, ROLE, VISIBLE) VALUES (?, ?, 2, ?)",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, user.getId());
                                setString(2, user.getName());
                                setBoolean(3, user.getVisible());
                            }});
                }

                return null;
            }
        };
        t.execute();
    }
    public void syncCreditmemo(final String BPValue,final Double OpenAmount) throws BasicException {

        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {
                // Sync the Customer in a transaction

                // Try to update
	        // Add the field TaxID to sync...
                if (new PreparedSentence(s,
                            "UPDATE CUSTOMERS SET curdebt=0, maxdebt = ? WHERE trim(taxid) = trim(?)",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setDouble(1, OpenAmount);
                                setString(2, BPValue);
                                
                            }}) == 0) {

                    // If not updated, try to insert
                   /* new PreparedSentence(s,
                            "INSERT INTO PEOPLE(ID, NAME, ROLE, VISIBLE) VALUES (?, ?, 2, " + s.DB.TRUE() + ")",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, user.getId());
                                setString(2, user.getName());
                            }});*/
                }

                return null;
            }
        };
        t.execute();
    }
    public void syncResendOrders(final int ticketid) throws BasicException {

        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {
                // Sync the Customer in a transaction

                // Try to update
	        // Add the field TaxID to sync...
                if (new PreparedSentence(s,
                            "UPDATE tickets SET Status=0 WHERE ticketid=? ",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setInt(1, ticketid);
                            }}) == 0) {

                }

                return null;
            }
        };
        t.execute();
    }
    
    public void syncProductsBefore() throws BasicException {
        new StaticSentence(s, "DELETE FROM PRODUCTS_CAT").exec();
    }
    public void syncProductsAfter() throws BasicException {
        new StaticSentence(s, "INSERT INTO PRODUCTS_CAT SELECT ID,NULL FROM PRODUCTS").exec();
    }
    
    
    public void syncTaxCategory(final TaxCategoryInfo taxcat) throws BasicException {
        
        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {
                // Sync the Tax in a transaction
                
                // Try to update                
                if (new PreparedSentence(s, 
                            "UPDATE TAXCATEGORIES SET NAME = ?  WHERE ID = ? OR TRIM(regexp_replace(NAME,'\\t','')) = ?",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, taxcat.getName());
                                setString(2, taxcat.getID());  
                                setString(3, taxcat.getName());
                            }}) == 0) {
                       
                    // If not updated, try to insert
                    new PreparedSentence(s, 
                            "INSERT INTO TAXCATEGORIES(ID, NAME) VALUES (?, ?)", 
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, taxcat.getID());
                                setString(2, taxcat.getName());
                            }});
                }
                
                return null;
            }
        };
        t.execute();                   
    }
    public void syncLocations(final String location_id,final String location_name) throws BasicException {
        
        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {
                // Sync the Tax in a transaction
                
                // Try to update                
                if (new PreparedSentence(s, 
                            "UPDATE LOCATIONS SET NAME = ?  WHERE ID = ?",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, location_name);
                                setString(2, location_id);                                    
                            }}) == 0) {
                       
                    // If not updated, try to insert
                    new PreparedSentence(s, 
                            "INSERT INTO LOCATIONS(ID, NAME) VALUES (?, ?)", 
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, location_id);
                                setString(2, location_name);
                            }});
                }
                
                return null;
            }
        };
        t.execute();                   
    }
    public void syncTax(final TaxInfo tax) throws BasicException {
        
        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {
                // Sync the Tax in a transaction
                
                // Try to update                
                if (new PreparedSentence(s, 
                            "UPDATE TAXES SET NAME = ?, CATEGORY = ?, CUSTCATEGORY = ?, PARENTID = ?, RATE = ?, RATECASCADE = ? WHERE ID = ? OR TRIM(regexp_replace(NAME,'\\t','')) = ?",
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, tax.getName());
                                setString(2, tax.getTaxCategoryID());
                                setString(3, tax.getTaxCustCategoryID());
                                setString(4, tax.getParentID());
                                setDouble(5, tax.getRate());
                                setBoolean(6, tax.isCascade());
                                setString(7, tax.getId()); 
                                setString(8, tax.getName());
                            }}) == 0) {
                       
                    // If not updated, try to insert
                    new PreparedSentence(s, 
                            "INSERT INTO TAXES(ID, NAME, CATEGORY, CUSTCATEGORY, PARENTID, RATE, RATECASCADE) VALUES (?, ?, ?, ?, ?, ?, ?)", 
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                setString(1, tax.getId());
                                setString(2, tax.getName());
                                setString(3, tax.getTaxCategoryID());
                                setString(4, tax.getTaxCustCategoryID());
                                setString(5, tax.getParentID());                                
                                setDouble(6, tax.getRate());
                                setBoolean(7, tax.isCascade());
                            }});
                }
                
                return null;
            }
        };
        t.execute();                   
    }
    
    public void syncCategory(final CategoryInfo cat) throws BasicException {
        
        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {
                // Sync the Category in a transaction
                
                // Try to update
                if (new PreparedSentence(s, 
                            //"UPDATE CATEGORIES SET NAME = ?, IMAGE = ? WHERE ID = ? OR TRIM(regexp_replace(NAME,'\\t','')) = ?", 
                            "UPDATE CATEGORIES SET NAME = ? WHERE ID = ? OR TRIM(regexp_replace(NAME,'\\t','')) = ?", 
                            SerializerWriteParams.INSTANCE
                            ).exec(new DataParams() { public void writeValues() throws BasicException {
                                 setString(1, cat.getName());
                                 //setBytes(2, ImageUtils.writeImage(cat.getImage()));
                                 setString(2, cat.getID());
                                 setString(3, cat.getName());
                            }}) == 0) {
                       
                    // If not updated, try to insert
                    new PreparedSentence(s, 
                        //"INSERT INTO CATEGORIES(ID, NAME, IMAGE) VALUES (?, ?, ?)",
                          "INSERT INTO CATEGORIES(ID, NAME) VALUES (?, ?)",
                        SerializerWriteParams.INSTANCE
                        ).exec(new DataParams() { public void writeValues() throws BasicException {
                            setString(1, cat.getID());
                            setString(2, cat.getName());
                            //setBytes(3, ImageUtils.writeImage(cat.getImage()));
                        }});
                }
                return null;        
            }
        };
        t.execute();        
    }    
    
    public void syncProduct(final ProductInfoExt prod) throws BasicException {
        
        Transaction t;
        t = new Transaction(s) {
    public Object transact() throws BasicException {
    // Sync the Product in a transaction
    
    // Try to update
    if (new PreparedSentence(s, 
                //"UPDATE PRODUCTS SET REFERENCE = ?, CODE = ?, NAME = ?, PRICEBUY = ?, PRICESELL = ?, CATEGORY = ?, TAXCAT = ?, IMAGE = ?, STOCKVOLUME = ?, DISPLAY = ? WHERE ID = ? OR TRIM(regexp_replace(REFERENCE,'\\t','')) = ?",
                "UPDATE PRODUCTS SET REFERENCE = ?, CODE = ?, NAME = ?, PRICEBUY = ?, PRICESELL = ?, CATEGORY = ?, TAXCAT = ?, STOCKVOLUME = ?, DISPLAY = ? WHERE ID = ? OR TRIM(regexp_replace(REFERENCE,'\\t','')) = ?",
                SerializerWriteParams.INSTANCE
                ).exec(new DataParams() { public void writeValues() throws BasicException {
                    setString(1, prod.getReference());
                    setString(2, prod.getCode());
                    setString(3, prod.getName());
                    setDouble(4, prod.getPriceBuy());
                    setDouble(5, prod.getPriceSell());
                    setString(6, prod.getCategoryID());
                    setString(7, prod.getTaxCategoryID());
                    //setBytes(8, ImageUtils.writeImage(prod.getImage()));
                    setDouble(8, prod.getStockVolume());
                    setString(9, prod.getDisplay());
                    setString(10, prod.getID());
                    setString(11, prod.getReference());
                }}) == 0) 
    {
        // If not updated, try to insert
        new PreparedSentence(s, 
                //"INSERT INTO PRODUCTS (ID, REFERENCE, CODE, NAME, ISCOM, ISSCALE, PRICEBUY, PRICESELL, CATEGORY, TAXCAT, IMAGE, STOCKCOST, STOCKVOLUME, DISPLAY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "INSERT INTO PRODUCTS (ID, REFERENCE, CODE, NAME, ISCOM, ISSCALE, PRICEBUY, PRICESELL, CATEGORY, TAXCAT, STOCKCOST, STOCKVOLUME, DISPLAY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                SerializerWriteParams.INSTANCE
                ).exec(new DataParams() { public void writeValues() throws BasicException {
                    setString(1, prod.getID());
                    setString(2, prod.getReference());
                    setString(3, prod.getCode());
                    setString(4, prod.getName());
                    setBoolean(5, prod.isCom());
                    setBoolean(6, prod.isScale());
                    setDouble(7, prod.getPriceBuy());
                    setDouble(8, prod.getPriceSell());
                    setString(9, prod.getCategoryID());
                    setString(10, prod.getTaxCategoryID());
                    //setBytes(11, ImageUtils.writeImage(prod.getImage()));
                    setDouble(11, 0.0);
                    setDouble(12, prod.getStockVolume());
                    setString(13, prod.getDisplay());                               
                }});
           }
        return null;        
    }
};
        t.execute();     
    }
// red1 - TicketInfo check for DataRead.getDataField().length > 9  (R.ATTRIBUTES, inserted, c.taxid jumps over)
    public List getTickets() throws BasicException {
        return new PreparedSentence(s
                , "SELECT T.ID, T.TICKETTYPE, T.TICKETID, R.DATENEW, R.MONEY, R.ATTRIBUTES, P.ID, P.NAME, C.ID, "
                        + "C.SEARCHKEY, C.NAME, C.TAXID "
                        + "FROM RECEIPTS R JOIN TICKETS T ON R.ID = T.ID "
                        + "LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                        + "LEFT OUTER JOIN CUSTOMERS C ON T.CUSTOMER = C.ID WHERE (T.TICKETTYPE = 0 OR T.TICKETTYPE = 1) AND T.STATUS = 2"
                , null
                , new SerializerReadClass(TicketInfo.class)).list();
    }
    public List getTicketsFiscal() throws BasicException {
        return new PreparedSentence(s
                , "SELECT T.ID, T.TICKETTYPE, T.TICKETID, R.DATENEW, R.MONEY, R.ATTRIBUTES, P.ID, P.NAME, C.ID, "
                        + "C.SEARCHKEY, C.NAME, C.TAXID, T.FISCALPRINT_SERIAL, T.FISCAL_INVOICENUMBER, T. FISCAL_ZREPORT "
                        + "FROM RECEIPTS R JOIN TICKETS T ON R.ID = T.ID "
                        + "LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                        + "LEFT OUTER JOIN CUSTOMERS C ON T.CUSTOMER = C.ID WHERE (T.TICKETTYPE = 0 OR T.TICKETTYPE = 1) AND T.STATUS = 2"
                , null
                , new SerializerReadClass(TicketInfo.class)).list();
    }
    /**Check if there is a process sending tickets by cheking database status in tickets. 
     * 0 not sync, 1 sync, 2 sync in progress
     * @return List of tickets in process of sync
     * @throws BasicException 
     */
    public List getTicketsSync() throws BasicException {
        return new PreparedSentence(s
                , "SELECT T.ID, T.TICKETTYPE, T.TICKETID, R.DATENEW, R.MONEY, R.ATTRIBUTES, P.ID, P.NAME, C.ID, "
                        + "C.SEARCHKEY, C.NAME, C.TAXID  "
                        + "FROM RECEIPTS R JOIN TICKETS T ON R.ID = T.ID "
                        + "LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                        + "LEFT OUTER JOIN CUSTOMERS C ON T.CUSTOMER = C.ID WHERE (T.TICKETTYPE = 0 OR T.TICKETTYPE = 1) AND T.STATUS = 2"
                , null
                , new SerializerReadClass(TicketInfo.class)).list();
    }
    public List getTicketsSyncFiscal() throws BasicException {
        return new PreparedSentence(s
                , "SELECT T.ID, T.TICKETTYPE, T.TICKETID, R.DATENEW, R.MONEY, R.ATTRIBUTES, P.ID, P.NAME, C.ID, "
                        + "C.SEARCHKEY, C.NAME, C.TAXID, T.FISCALPRINT_SERIAL, T.FISCAL_INVOICENUMBER, T. FISCAL_ZREPORT "
                        + "FROM RECEIPTS R JOIN TICKETS T ON R.ID = T.ID "
                        + "LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                        + "LEFT OUTER JOIN CUSTOMERS C ON T.CUSTOMER = C.ID WHERE (T.TICKETTYPE = 0 OR T.TICKETTYPE = 1) AND T.STATUS = 2"
                , null
                , new SerializerReadClass(TicketInfo.class)).list();
    }
    public List getTicketLines(final String ticket) throws BasicException {
        return new PreparedSentence(s
                //, "SELECT L.TICKET, L.LINE, L.PRODUCT, L.ATTRIBUTESETINSTANCE_ID, L.UNITS, L.PRICE, T.ID, T.NAME, T.CATEGORY, T.VALIDFROM, T.CUSTCATEGORY, T.PARENTID, T.RATE, T.RATECASCADE, T.RATEORDER, L.ATTRIBUTES " +
                , "SELECT L.TICKET, L.LINE, L.PRODUCT, L.ATTRIBUTESETINSTANCE_ID, L.UNITS, L.PRICE, T.ID, T.NAME, T.CATEGORY, T.CUSTCATEGORY, T.PARENTID, T.RATE, T.RATECASCADE, T.RATEORDER, L.ATTRIBUTES " +
                "FROM TICKETLINES L, TAXES T WHERE L.TAXID = T.ID AND L.TICKET = ? ORDER BY L.LINE"
//  red1       , "SELECT L.TICKET, L.LINE, L.PRODUCT, L.UNITS, L.PRICE, T.ID, T.NAME, T.CATEGORY, T.CUSTCATEGORY, T.PARENTID, T.RATE, T.RATECASCADE, T.RATEORDER, L.ATTRIBUTES " +
//  red1         "FROM TICKETLINES L, TAXES T WHERE L.TAXID = T.ID AND L.TICKET = ?"
                , SerializerWriteString.INSTANCE
                , new SerializerReadClass(TicketLineInfo.class)).list(ticket);
    }
    public List getTicketPayments(final String ticket) throws BasicException {
        return new PreparedSentence(s
                , "SELECT TOTAL, PAYMENT FROM PAYMENTS WHERE RECEIPT = ?"
                , SerializerWriteString.INSTANCE
                , new SerializerRead() {
                    public Object readValues(DataRead dr) throws BasicException {
                        return new PaymentInfoTicket(
                                dr.getDouble(1),
                                dr.getString(2));
                    }                
                }).list(ticket);
    }    

    public CustomerInfoExt getTicketCustomer(final String ticket) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s
                , "SELECT C.ID, C.TAXID, C.SEARCHKEY, C.NAME, C.CARD, C.TAXCATEGORY, C.NOTES, C.MAXDEBT, C.VISIBLE, C.CURDATE, C.CURDEBT" +
                  ", C.FIRSTNAME, C.LASTNAME, C.EMAIL, C.PHONE, C.PHONE2, C.FAX" +
                  ", C.ADDRESS, C.ADDRESS2, C.POSTAL, C.CITY, C.REGION, C.COUNTRY" +
                " FROM CUSTOMERS C INNER JOIN TICKETS T ON C.ID = T.CUSTOMER WHERE T.ID = ?"
                , SerializerWriteString.INSTANCE
                , CustomerInfoExt.getSerializerRead()
                ).find(ticket);
    }    

    public TaxCategoryInfo getTaxCategoryInfoByName(final String name) throws BasicException {
        return (TaxCategoryInfo) new PreparedSentence(s
                , "SELECT ID, NAME FROM TAXCATEGORIES WHERE NAME = ?"
                , SerializerWriteString.INSTANCE
                , new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
                     return new TaxCategoryInfo(
                        dr.getString(1), 
                        dr.getString(2));
            }}).find(name);
    }
    public final CategoryInfo getCategoryInfoByName(final String name) throws BasicException {
        return (CategoryInfo) new PreparedSentence(s
        , "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME "
                + "FROM CATEGORIES "
                + "WHERE NAME = ? "
                + "ORDER BY NAME"
        , SerializerWriteString.INSTANCE
        , CategoryInfo.getSerializerRead()).find(name);
    }
    public void execTicketUpdate() throws BasicException {
        new StaticSentence(s, "UPDATE TICKETS SET STATUS = 1 WHERE STATUS = 2").exec();
    }
    public void execTicketUpdateError() throws BasicException {
        new StaticSentence(s, "UPDATE TICKETS SET STATUS = 0 WHERE STATUS = 2").exec();
    }
    public void execClosedCashUpdate() throws BasicException {
        new StaticSentence(s, "UPDATE CLOSEDCASH SET STATUS = 1 WHERE STATUS = 2 AND DATEEND IS NOT NULL ").exec();
    }
    public void execClosedCashUpdateError() throws BasicException {
        new StaticSentence(s, "UPDATE CLOSEDCASH SET STATUS = 0 WHERE STATUS = 2 AND DATEEND IS NOT NULL").exec();
    }
    public void setTicketsInProcess() throws BasicException {
        new StaticSentence(s, "UPDATE TICKETS SET STATUS = 2 WHERE STATUS = 0  ").exec();
    }
    public void setTicketsInProcessFiscal() throws BasicException {
        new StaticSentence(s, "UPDATE TICKETS SET STATUS = 2 WHERE STATUS = 0 AND FISCAL_INVOICENUMBER IS NOT NULL ").exec();
    }
    public void resendTickets() throws BasicException {
        new StaticSentence(s, "UPDATE TICKETS SET STATUS = 0 WHERE ID IN (SELECT ID FROM RECEIPTS WHERE DATENEW BETWEEN date_trunc('day', now()) AND now()) AND FISCAL_INVOICENUMBER IS NOT NULL ").exec();
    }
    public void resendTickets(final Timestamp OrdersSyncDateFrom,final Timestamp OrdersSyncDateTo) throws BasicException {
        new PreparedSentence(s, "UPDATE TICKETS SET STATUS = 0 WHERE ID IN (SELECT ID FROM RECEIPTS WHERE DATENEW BETWEEN ? AND ?) AND FISCAL_INVOICENUMBER IS NOT NULL ", SerializerWriteParams.INSTANCE)
                .exec(new DataParams() { public void writeValues() throws BasicException {
                    setTimestamp(1, OrdersSyncDateFrom);
                    setTimestamp(2, OrdersSyncDateTo);
                    
                }});
        //new StaticSentence(s, ).exec();
    }
    public void setClosedCashInProcess() throws BasicException {
        new StaticSentence(s, "UPDATE CLOSEDCASH SET STATUS = 2 WHERE STATUS = 0 AND DATEEND IS NOT NULL").exec();
    }

    public List getClosedCash() throws BasicException {
    return new PreparedSentence(s
            , "SELECT MONEY, HOSTSEQUENCE, DATESTART, DATEEND, DIFFERENCE, PERSON FROM CLOSEDCASH WHERE STATUS = 2 AND DATEEND IS NOT NULL"
            , null
            , new SerializerRead() {
                public Object readValues(DataRead dr) throws BasicException {
                    return new ClosedCashInfo(
                            dr.getString(1),
                            dr.getString(1),
                            dr.getInt(2),
                            dr.getTimestamp(3),
                            dr.getTimestamp(4),
                            dr.getDouble(5),
                            dr.getString(6));
                }                
            }).list();
    }
    public List getClosedCashSync() throws BasicException {
    return new PreparedSentence(s
            , "SELECT MONEY, HOSTSEQUENCE, DATESTART, DATEEND, DIFFERENCE, PERSON FROM CLOSEDCASH WHERE STATUS = 2 AND DATEEND IS NOT NULL"
            , null
            , new SerializerRead() {
                public Object readValues(DataRead dr) throws BasicException {
                    return new ClosedCashInfo(
                            dr.getString(1),
                            dr.getString(1),
                            dr.getInt(2),
                            dr.getTimestamp(3),
                            dr.getTimestamp(4),
                            dr.getDouble(5),
                            dr.getString(6));
                }                
            }).list();
    }
    
        public List getCreditNote(String receiptId) throws BasicException {
        return new PreparedSentence(s
                ,   "SELECT A.NUMBER, A.ID, A.RECEIPT, A.TOTAL, A.AVALIABLE, A.DATENEW, A.BRANCH " +
                    "FROM CREDITNOTE A " +
                    "WHERE A.RECEIPT = ? "
            , SerializerWriteString.INSTANCE
            , new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
                     return new CreditNoteInfo(
                        dr.getInt(1), 
                        dr.getString(2),
                        dr.getString(3),
                        dr.getDouble(4), 
                        dr.getDouble(5),
                        dr.getTimestamp(6),
                        dr.getString(7));
            }}).list(receiptId);
        
    }
    public final Boolean updateTicketFiscalTheFactory(final TicketInfo ticket,File fileStatus) throws BasicException, FileNotFoundException, IOException, ParseException {
        FileReader reader = new FileReader(fileStatus);
        BufferedReader br = new BufferedReader(reader); 
        String status; 
        if((status = br.readLine()) == null) {
            return false;
        }
        final int zlength = status.substring(47,51).length();
        final int znumber = Integer.parseInt(status.substring(47,51))+1;
        final String fiscalprint_serial = status.substring(66,76);
        final String fiscal_invoicenumber = status.substring(21,29);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
        final Date date= formatter.parse(status.substring(82,84)+"/"+status.substring(84,86)+"/"+status.substring(86,88)
                +" "+status.substring(76,78)+":"+status.substring(78,80)+":"+status.substring(80,82) );
        
        SentenceExec ticketFiscalUpdate = new PreparedSentence(s
                , "UPDATE TICKETS SET fiscal_invoicenumber=?, fiscalprint_serial=?, fiscal_zreport=? WHERE ID = ?"
                , SerializerWriteParams.INSTANCE);
                        ticketFiscalUpdate.exec(new DataParams() { public void writeValues() throws BasicException {
                            setString(1, fiscal_invoicenumber);
                            setString(2, fiscalprint_serial);
                            setString(3, String.format("%0" + String.valueOf(zlength) +"d", znumber));
                            setString(4, ticket.getId());
                       }});
        
        SentenceExec receiptFiscalUpdate = new PreparedSentence(s
                , "UPDATE RECEIPTS SET DATENEW=? WHERE ID = ?"
                , SerializerWriteParams.INSTANCE);
                        receiptFiscalUpdate.exec(new DataParams() { public void writeValues() throws BasicException {
                            setTimestamp(1, new Timestamp(date.getTime()));
                            setString(2, ticket.getId());
                       }});
        return true;
    }
    //Migrate functions 
    public TicketInfo getLastTicketOfMachineFiscal() throws BasicException{
        final Properties m_propsconfig =  new Properties();    
                File file =  new File(new File(System.getProperty("user.home")), AppLocal.APP_ID + ".properties");
                try {
                    InputStream in = new FileInputStream(file);
                    if (in != null) {
                        m_propsconfig.load(in);
                        in.close();
                    }
                } catch (IOException e){
   
                }
        TicketInfo ticket = (TicketInfo) new PreparedSentence(s
                , " SELECT T.ID, T.TICKETTYPE, T.TICKETID, R.DATENEW, R.MONEY, R.ATTRIBUTES, P.ID, P.NAME, T.CUSTOMER, "
                        + " C.SEARCHKEY, C.NAME, C.TAXID, T.FISCALPRINT_SERIAL, T.FISCAL_INVOICENUMBER, T. FISCAL_ZREPORT "
                        + " FROM RECEIPTS R JOIN TICKETS T ON R.ID = T.ID LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                        + " LEFT OUTER JOIN CUSTOMERS C ON T.CUSTOMER = C.ID "
                        + " WHERE T.MACHINENAME = ? AND TRIM(T.FISCAL_INVOICENUMBER) is null "
                , SerializerWriteParams.INSTANCE
                , new SerializerReadClass(TicketInfo.class))
                .find(new DataParams() { public void writeValues() throws BasicException {
                   setString(1, m_propsconfig.getProperty("machine.hostname"));
                }});
        
        return ticket;
    }
    public final TicketInfo loadTicketFiscal(final int tickettype, final int ticketid) throws BasicException {
        TicketInfo ticket = (TicketInfo) new PreparedSentence(s
                , "SELECT T.ID, T.TICKETTYPE, T.TICKETID, R.DATENEW, R.MONEY, R.ATTRIBUTES, P.ID, P.NAME, T.CUSTOMER, "
                        + " C.SEARCHKEY, C.NAME, C.TAXID, T.FISCALPRINT_SERIAL, T.FISCAL_INVOICENUMBER, T. FISCAL_ZREPORT "
                        + " FROM RECEIPTS R JOIN TICKETS T ON R.ID = T.ID LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                        + " LEFT OUTER JOIN CUSTOMERS C ON T.CUSTOMER = C.ID "
                        + " WHERE T.TICKETTYPE = ? AND T.TICKETID = ? "
                , SerializerWriteParams.INSTANCE
                , new SerializerReadClass(TicketInfo.class))
                .find(new DataParams() { public void writeValues() throws BasicException {
                    setInt(1, tickettype);
                    setInt(2, ticketid);
                }});
        if (ticket != null) {
            DataLogicSales logicSales= new DataLogicSales();
            logicSales.init(this.s);
            String customerid = ticket.getCustomerId();
            ticket.setCustomer(customerid == null
                    ? null
                    : logicSales.loadCustomerExt(customerid));

            ticket.setLines(new PreparedSentence(s
                , "SELECT L.TICKET, L.LINE, L.PRODUCT, L.ATTRIBUTESETINSTANCE_ID, L.UNITS, L.PRICE, T.ID, T.NAME, T.CATEGORY, T.VALIDFROM, T.CUSTCATEGORY, T.PARENTID, T.RATE, T.RATECASCADE, T.RATEORDER, L.ATTRIBUTES " +
                  "FROM TICKETLINES L, TAXES T WHERE L.TAXID = T.ID AND L.TICKET = ? ORDER BY L.LINE"
                , SerializerWriteString.INSTANCE
                , new SerializerReadClass(TicketLineInfo.class)).list(ticket.getId()));
            ticket.setPayments(new PreparedSentence(s
                , "SELECT PAYMENT, TOTAL, TRANSID FROM PAYMENTS WHERE RECEIPT = ?"
                , SerializerWriteString.INSTANCE
                , new SerializerReadClass(PaymentInfoTicket.class)).list(ticket.getId()));
        }
        return ticket;
    }
}
