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

package com.ghintech.fiscalprint;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataParams;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.SerializerWriteParams;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.payment.PaymentInfoTicket;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


/**
 *
 * @author adrianromero
 * Created on 5 de marzo de 2007, 19:56
 * @contributor Sergio Oropeza - Double Click Sistemas - Venezuela - soropeza@dcs.net.ve, info@dcs.net.ve
 *
 */
public class DataLogicFiscal extends BeanFactoryDataSingle {
    
    protected Session s;
    private PreparedStatement pstmt;
    private String SQL;
    private ResultSet rs;
    private Statement stmt;
    private Connection con;

    /** Creates a new instance of DataLogicIntegration */
    public DataLogicFiscal() {
        
    }
    
    public void init(Session s) {
        this.s = s;
        try{
            
            con=s.getConnection();                      
        }
        catch (SQLException e){
            System.out.print("No session or connection");
        }
    }
     
    
    public final String findFiscalNumber(final int ticketid,final int tickettype) throws BasicException {
        PreparedSentence p = new PreparedSentence(s
                , "SELECT FISCALNUMBER FROM TICKETS WHERE TICKETID=? AND TICKETTYPE=?"
                //, new SerializerWriteBasic(new Datas[] {
                //Datas.OBJECT, Datas.INT})
                , SerializerWriteParams.INSTANCE
                , SerializerReadString.INSTANCE);
        String fNumber = (String) p.find(new DataParams() {@Override
                                        public void writeValues() throws BasicException {
                                            setInt(1, ticketid);
                                            setInt(2, tickettype);
                                        }});
        return fNumber;
    }
    public final Boolean updateTicketFiscalCopyTheFactory(final TicketInfo ticket,final String fNumber) throws BasicException, FileNotFoundException, IOException, ParseException {
                SentenceExec ticketFiscalUpdate = new PreparedSentence(s
                , "UPDATE TICKETS SET fiscalnumber=? WHERE ID = ?"
                , SerializerWriteParams.INSTANCE);
                        ticketFiscalUpdate.exec(new DataParams() { public void writeValues() throws BasicException {
                            setString(1, fNumber);
                            setString(2, ticket.getId());
                       }});
        
        return true;
    }
    public final Boolean updateTicketFiscalTheFactory(final TicketInfo ticket,File fileStatus) throws BasicException, FileNotFoundException, IOException, ParseException {
        FileReader reader = new FileReader(fileStatus);
        BufferedReader br = new BufferedReader(reader); 
        String status; 
        if((status = br.readLine()) == null) {
            return false;
        }
       
        String aux = status.substring(21,29);
        if (ticket.getTicketType()==1){
            aux = status.substring(34,42);
        }
        final String fiscalnumber = aux;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
        final Date date= formatter.parse(status.substring(122,124)+"/"+status.substring(124,126)+"/"+status.substring(126,128)
                +" "+status.substring(116,118)+":"+status.substring(118,120)+":"+status.substring(120,122));
        
        SentenceExec ticketFiscalUpdate = new PreparedSentence(s
                , "UPDATE TICKETS SET fiscalnumber=? WHERE ID = ?"
                , SerializerWriteParams.INSTANCE);
                        ticketFiscalUpdate.exec(new DataParams() { public void writeValues() throws BasicException {
                            setString(1, fiscalnumber);
                            setString(2, ticket.getId());
                       }});
        
        /*SentenceExec receiptFiscalUpdate = new PreparedSentence(s
                , "UPDATE RECEIPTS SET DATENEW=? WHERE ID = ?"
                , SerializerWriteParams.INSTANCE);
                        receiptFiscalUpdate.exec(new DataParams() { public void writeValues() throws BasicException {
                            setTimestamp(1, new Timestamp(date.getTime()));
                            setString(2, ticket.getId());
                       }});*/
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
    public String getTableDetails (String ticketID){
       try{
            SQL = "SELECT NAME FROM PLACES WHERE TICKETID='"+ ticketID + "'";   
            stmt = (Statement) con.createStatement();  
            rs = stmt.executeQuery(SQL);
            if (rs.next()){
                String name =rs.getString("NAME");
                return(name);
            }    
        }catch(Exception e){
        
        }
        return "";
   }
}
