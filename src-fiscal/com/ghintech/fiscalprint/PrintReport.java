//    Openbravo POS is a point of sales application designed for touch screens.
//    http://www.openbravo.com/product/pos
//    Copyright (c) 2007 openTrends Solucions i Sistemes, S.L
//    Modified by Openbravo SL on March 22, 2007
//    These modifications are copyright Openbravo SL
//    Author/s: A. Romero 
//    You may contact Openbravo SL at: http://www.openbravo.com
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

import com.ghintech.fiscalprint.bematech.BemaFI;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.ProcessAction;
import com.sun.jna.Native;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class PrintReport implements ProcessAction {
    private final String reportType;
    private final String invoiceFolder;
    private final String spoolerFolder;
    private final String fileName;
    private final String vendor;
    private final String library;

    public PrintReport(String reportType, DataLogicSystem dlsystem) {
        this.reportType = reportType;
        Properties prop = dlsystem.getResourceAsProperties("fiscalprint.properties");
        String invoiceFolderT = null;
        String spoolerFolderT = null;
        String fileNameT = null;
        String vendorT = prop.getProperty("vendor");
        String libraryT = null;
        if (prop.getProperty("vendor").compareTo("thefactory") == 0) {
            invoiceFolderT = prop.getProperty("invoiceFolder");
            spoolerFolderT = prop.getProperty("spoolerFolder");
            fileNameT = prop.getProperty("fileName");
        } else if (prop.getProperty("vendor").compareTo("bematech") == 0) {
            libraryT = prop.getProperty("library");
        }
        this.invoiceFolder = invoiceFolderT;
        this.spoolerFolder = spoolerFolderT;
        this.fileName = fileNameT;
        this.vendor = vendorT;
        this.library = libraryT;
    }

    @Override
    public MessageInf execute() throws BasicException {
        try {
            if (this.vendor.compareTo("thefactory") == 0) {
                File file = new File(this.invoiceFolder + this.fileName);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(this.reportType);
                writer.flush();
                writer.close();
                String filePath = this.spoolerFolder + "IntTFHKA.exe SendFileCmd(" + this.invoiceFolder + this.fileName + ")";
                Process p = Runtime.getRuntime().exec(filePath);
                p.waitFor();
                boolean checkprinter = false;
                File fileStatus = new File(this.spoolerFolder + "Status_Error.txt");
                if (fileStatus.exists()) {
                    String status;
                    FileReader reader = new FileReader(fileStatus);
                    BufferedReader br = new BufferedReader(reader);
                    while ((status = br.readLine()) != null) {
                        String[] statusError = status.split("\\t");
                        if (statusError[2].compareTo("0") != 0) continue;
                        checkprinter = true;
                    }
                    reader.close();
                }
                if (!checkprinter) {
                    JOptionPane.showMessageDialog(null, "No se pudo imprimir el ticket correctamente", "POS", JOptionPane.PLAIN_MESSAGE);
                    return new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotprint"));
                }
            } else if (this.vendor.compareTo("bematech") == 0) {
                BemaFI print = (BemaFI)Native.loadLibrary((String)this.library, (Class)BemaFI.class);
                int iRetorno = this.reportType.compareTo("I0X") == 0 ? print.Bematech_FI_LecturaX() : print.Bematech_FI_ReduccionZ("", "");
                if (iRetorno != 1) {
                    JOptionPane.showMessageDialog(null, "No se pudo imprimir el ticket correctamente", "POS", JOptionPane.PLAIN_MESSAGE);
                    return new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotprint"));
                }
            }
        }
        catch (IOException | InterruptedException ex) {
            Logger.getLogger(PrintReport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new MessageInf(MessageInf.SGN_SUCCESS, AppLocal.getIntString("message.printok"));
    }
}
