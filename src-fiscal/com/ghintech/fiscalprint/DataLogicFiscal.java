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
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.SerializerWriteParams;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.ticket.TicketInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *
 * @author adrianromero
 * Created on 5 de marzo de 2007, 19:56
 * @contributor Sergio Oropeza - Double Click Sistemas - Venezuela - soropeza@dcs.net.ve, info@dcs.net.ve
 *
 */
public class DataLogicFiscal extends BeanFactoryDataSingle {
    
    protected Session s;

    /** Creates a new instance of DataLogicIntegration */
    public DataLogicFiscal() {
    }
    
    public void init(Session s) {
        this.s = s;
    }
     
    
    public final String findFiscalNumber(final int ticketid) throws BasicException {
        PreparedSentence p = new PreparedSentence(s
                , "SELECT FISCALNUMBER FROM TICKETS WHERE TICKETID=?"
                //, new SerializerWriteBasic(new Datas[] {
                //Datas.OBJECT, Datas.INT})
                , SerializerWriteParams.INSTANCE
                , SerializerReadString.INSTANCE);
        String fNumber = (String) p.find(new DataParams() {@Override
                                        public void writeValues() throws BasicException {
                                            setInt(1, ticketid);
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
}
