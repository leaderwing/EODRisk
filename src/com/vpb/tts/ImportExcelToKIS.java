/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vpb.tts;

import au.com.bytecode.opencsv.CSVReader;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import com.vpb.tts.config.DBConnection;
import com.vpb.tts.config.DateUtil;
import com.vpb.tts.config.ReadImportFiles;
import com.vpb.tts.constant.ColOrderConstant;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_NULL_AND_BLANK;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author quynx
 */
public class ImportExcelToKIS {

    public static void main(String[] args) throws IOException, FileNotFoundException, SQLException {
        String excel_file = "";
        char seprator  = ',';
        ReadImportFiles readImp = new ReadImportFiles();
        InputStream inputStream = ImportExcelToKIS.class.getClassLoader().getResourceAsStream("MigrationProperties.properties");
        Properties p = new Properties();
        p.load(inputStream);
        inputStream.close();
        String sfpt = p.getProperty("SFTP_PATH");
        String output_path = p.getProperty("SFTP_OUTPUT");
        String destination_path = p.getProperty("SFTP_DESTINATION");
        CSVReader csvReader = null;
        String currentDate = DateUtil.getStringDate(new Date());
        File[] listFiles = new File(sfpt).listFiles();
        Map mappingFloating = getMappingFile(output_path+"//static//floating_mapping.txt");
        Map mappingFloatingRate = getMappingFile(output_path+"//static//floatingRate_mapping.txt");
        Map mappingSpot = getMappingFile(output_path+"//static//Spot_mapping.txt");
        Map mappingPoint = getMappingFile(output_path+"//static//point_mapping.txt");
        List<String> listColumnDealTbl =  readImp.readListFileNames("mappingColumnDealTbl.txt");
        List<CurvesModel> listCurvesRate = getListCurvesRates(p.getProperty("DB_URL"), p.getProperty("DB_USERNAME"), p.getProperty("DB_PASSWORD"));
        List<CurvesModel> listCurvesRateNR = getListCurvesRatesNR(p.getProperty("DB_URL"), p.getProperty("DB_USERNAME"), p.getProperty("DB_PASSWORD"));
        if(listFiles.length == 0 ){
            System.err.println("[ERROR]No imported files found!");
            return;
        } else {
        for (File file: listFiles) {
            try {
            System.out.println("Sleep 2 seconds.....");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ImportExcelToKIS.class.getName()).log(Level.SEVERE, null, ex);
            }
            String[] nextLine;
                try {
                csvReader = new CSVReader(new FileReader(file.getPath()), seprator);
                } catch (Exception e) {
                    e.printStackTrace();                   
                }
                String tableName = ReadImportFiles.getFileType(file.getName());
                if(StringUtils.isEmpty(tableName)) {
                 System.err.println("[ERROR] Filename "+file.getName()+" is wrong format.....");
                 break;
                }
                //validate data input
                
                String columnMappingDeal = listColumnDealTbl.get(ColOrderConstant.ColOrderConstant(tableName));
                String[] headers = columnMappingDeal.split(",");
                System.out.println("Convert KIS file "+tableName );
                String[] headerRow = csvReader.readNext();
                if (null == headerRow) {
                    throw new FileNotFoundException(
                    "No columns defined in given CSV file."
                    + "Please check the CSV file format.");
                    }
                String content = "";
                int row_num = 1;             
                Set dupeChecker = new HashSet();
                DecimalFormat df = new DecimalFormat("#.##############");
                outerloop:
                if(!"CurvesRates".equals(tableName)) {               
                while ((nextLine = csvReader.readNext()) != null) {
                    //Validate duplicate
                    if (!getDuplicates(dupeChecker,tableName,nextLine)) break outerloop;
                    
                    if (tableName.equals("FloatingRatesValues") && mappingFloating.get(nextLine[0]) == null)
                    {
                        System.err.println("[ERROR]: FLoatingRates is null with RIC Values: "+ nextLine[0]);
                        continue;
                    }
                    if (tableName.equals("FxSwapRevalPoints") && mappingPoint.get(nextLine[0]) == null)
                    {
                        System.err.println("[ERROR]: Points is null with RIC Values: "+ nextLine[0]);
                        continue;
                    }
                    if (tableName.equals("SpotQuotes") && mappingSpot.get(nextLine[0]) == null)
                    {
                        System.err.println("[ERROR]: SpotQuotes is null with RIC Values: "+ nextLine[0]);
                        continue;
                    }
                    if (tableName.equals("SpotQuotes") || tableName.equals("BondsQuotes") || tableName.equals("FloatingRatesValues")){
                    content += "########### Record no " + row_num + " ###########\nACTION UPD\n";
                    content += "TradeKast Y\nExecuteCustomProcs Y\n";
                    }
                    else{
                    content += "########### Record no " + row_num + " ###########\nACTION INS\n";
                    content += "TradeKast Y\nExecuteCustomProcs Y\n";
                    }
                    content += "TBL\t" + tableName +"\n";
                    int i = 0;
                    while (i < headers.length) {                      
                        String key = headers[i].replace("[", "").replace("]", "");
                        if (key.contains("TBL")) {
                            key = "TBL";
                            content += "EOT" + "\n";
                        } else if (key.contains("Users.Ref")) {
                            content += "EOT" + "\n";
                        }
                        if(tableName.equals("SpotQuotes"))
                        {                                                       
                            switch (i){
                                    case 0:
                                        content += key + "\t" + "R" + "\n";
                                        break;
                                    case 1:
                                        content += key + "\t" + DateUtil.getStringDateMMDDYYYY(new Date()) + "\n";
                                        break;
                                    case 2:
                                        content += key + "\t" + "0" + "\n";
                                        break;
                                    case 3:
                                        content += key + "\t" + "0" + "\n";
                                        break;
                                    case 4:
                                        content += key + "\t" + "0" + "\n";
                                        break;
                                    case 5:
                                        if (mappingSpot.get(nextLine[0]).equals("1")){
                                        content += key + "\t" + nextLine[7] + "\n";}
                                        else{
                                        content += key + "\t" + nextLine[5] + "\n";}
                                        break;
                                    case 6:
                                        if (mappingSpot.get(nextLine[0]).equals("1")){
                                        content += key + "\t" + nextLine[6] + "\n";}
                                    else
                                        {
                                        content += key + "\t" + nextLine[5] + "\n";}
                                        break;
                                    case 7:
                                        if (mappingSpot.get(nextLine[0]).equals("1")){
                                        content += key + "\t" + nextLine[5] + "\n";
                                        content += "EOT" + "\n";}
                                        else
                                        {
                                        content += key + "\t" + nextLine[5] + "\n";
                                        content += "EOT" + "\n";}  
                                        break;
                                    case 8:
                                        content += key + "\t" + "\"" + nextLine[3] + "\"" + "\n";                                        
                                        break;
                            }             
                        }else  if (tableName.equals("FxSwapRevalPoints")){
                            switch (i){
                                    case 0:
                                        content += key + "\t" + "R" + "\n";
                                        break;
                                    case 1:
                                        content += key + "\t" + DateUtil.getStringDateMMDDYYYY(new Date()) + "\n";
                                        break;
                                    case 2:
                                        content += key + "\t" + "0" + "\n";
                                        break;
                                    case 3:
                                        content += key + "\t" + "0" + "\n";
                                        break;
                                    case 4:
                                        content += key + "\t" + "0" + "\n";
                                        break;
                                    case 5:
                                        if(mappingPoint.get(nextLine[0]).equals("1")){
                                        content += key + "\t" + nextLine[7] + "\n";}
                                        else
                                        {
                                         content += key + "\t" + nextLine[5] + "\n";   
                                        }
                                        break;
                                    case 6:
                                        if(mappingPoint.get(nextLine[0]).equals("1")){
                                        content += key + "\t" + nextLine[5] + "\n";
                                        }else
                                        {
                                        content += key + "\t" + nextLine[5] + "\n";  
                                        }
                                        break;
                                    case 7:
                                        if(mappingPoint.get(nextLine[0]).equals("1")){
                                        content += key + "\t" + nextLine[6] + "\n";
                                        }else
                                        {
                                        content += key + "\t" + nextLine[5] + "\n";
                                        }
                                        break;
                                    case 8:
                                        content += "EOT" + "\n";
                                        content += key + "\t" + "\"" + nextLine[3] + "/" + nextLine[4] + "\"" + "\n";
                                        break;
                                    case 9:
                                        content += key + "\t" + "\""+ nextLine[2] + "\"" + "\n";
                                        break;
                            } 
                        }else if (tableName.equals("BondsQuotes")) {
                            switch (i){
                                    case 0:
                                        content += key + "\t" + "R" + "\n";
                                        break;
                                    case 1:
                                        content += key + "\t" + DateUtil.getStringDateMMDDYYYY(new Date()) + "\n";
                                        break;
                                    case 2:
                                        content += key + "\t" + "0" + "\n";
                                        break;
                                    case 3:
                                        content += key + "\t" + "0" + "\n";
                                        break;
                                    case 4:
                                        content += key + "\t" + nextLine[5] + "\n";
                                        break;
                                    case 5:
                                        content += key + "\t" + nextLine[5] + "\n";
                                        break;
                                    case 6:
                                        content += key + "\t" + "NULL" + "\n";
                                        break;
                                    case 7:
                                        content += key + "\t" + "0" + "\n";
                                        break;
                                    case 8:
                                        content += "EOT" + "\n";
                                        content += key + "\t" + "\"" + nextLine[1] + "\"" + "\n";
                                        break;
                                    case 9:
                                        content += key + "\t"  + "\n";
                                        break;
                            }
                        }else if (tableName.equals("FloatingRatesValues")) {
                            switch (i){
                                     case 0:
                                        content += key + "\t" + DateUtil.getStringDateMMDDYYYY(new Date()) + "\n";
                                        break;
                                    case 1:
                                        if(mappingFloatingRate.get(nextLine[0]).equals("CLOSE"))
                                        {content += key + "\t" + nextLine[5] + "\n";
                                        break;}
                                        else if (mappingFloatingRate.get(nextLine[0]).equals("BID"))
                                        {content += key + "\t" + nextLine[2] + "\n";   
                                        break;}
                                        else if (mappingFloatingRate.get(nextLine[0]).equals("ASK")) {
                                        content += key + "\t" + nextLine[3] + "\n";   
                                        break;  
                                        }else if (mappingFloatingRate.get(nextLine[0]).equals("MID")){
                                         content += key + "\t" + nextLine[4] + "\n";   
                                         break;   
                                        }
                                    case 2:
                                        content += key + "\t" + "U" + "\n";
                                        break;
                                    case 3:
                                        content += "EOT" + "\n";
                                        content += key + "\t" + "\"" + mappingFloating.get(nextLine[0]) + "\"" + "\n";
                                        break;
                            }
                        }                       
                      i++;
                    }
                    content += "EOR\n";
                    row_num ++;
                }                  
                writeToFileBase(content, destination_path,destination_path+"//"+tableName+"_"+currentDate+".txt");  
                }
                else {
                    //write curves file to list
                    Set<CurvesModel> rmrCurves = new HashSet<>();
                    content = "Curve short name, Tenor, Value" + "\n";
                    while ((nextLine = csvReader.readNext()) != null) {
                     CurvesModel curves = new CurvesModel();                  
                     content += nextLine[0] + "," + nextLine[1] + "," + nextLine[2] + "\n";   
                     curves.setCurve_name(nextLine[0]);
                     curves.setTenor(nextLine[1]);
                     if(!rmrCurves.add(curves))
                     {
                       System.err.println("[ERROR] Exist duplicated Record in Curves with value: " + nextLine[0] + "," + nextLine[1] +  " - Abort import file! ");
                       break outerloop;
                     }
                    }
                    //write hist rate to file
                    if( listCurvesRate.size() > 0 && listCurvesRateNR.size() > 0) {                        
                       for (CurvesModel curves: listCurvesRate) {
                           CurvesModel cloneCurves = new CurvesModel(curves.getCurve_name(),curves.getTenor());
                           if ( !rmrCurves.contains(cloneCurves)) {
                           content += curves.getCurve_name() + "," + curves.getTenor() + "," + curves.getValue() + "\n";
                           }
                       }
                       for (CurvesModel curves: listCurvesRateNR) {
                           CurvesModel cloneCurves = new CurvesModel(curves.getCurve_name(),curves.getTenor());
                           if ( !rmrCurves.contains(cloneCurves)) {
                           content += curves.getCurve_name() + "," + curves.getTenor() + "," + curves.getValue() + "\n";
                           }
                       }                      
                    }
                    writeToFileBase(content,output_path+"//"+currentDate,output_path+"//"+currentDate+"//"+"CurvesRates_"+currentDate+".csv");
                }
                //System.out.println(content);               
             copyAndRemoveFile(sfpt,output_path, file.getName());
        }
        catch (Exception e) {
               e.printStackTrace();
               continue;
          }    
        }
        }
    }

    private static synchronized void writeToFileBase(String Contents,String folder_path ,String filePath) throws IOException {
        System.out.println("Exported KIS files: "+folder_path);
        File directory = new File(folder_path);
        if(!directory.exists()) {
            directory.mkdir();
        }
        File f = new File(filePath);
        if (f.exists()) {
            System.out.println("File existed , delete file!");
            f.delete();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
        writer.write(Contents);
        writer.close();
    }
    private static Map getMappingFile(String filePath) throws FileNotFoundException, IOException {
    Map<String, String> map = new HashMap<String, String>();
    String line;
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    while ((line = reader.readLine()) != null)
    {
        String[] parts = line.split(",", 2);
        if (parts.length >= 2)
        {
            String key = parts[0];
            String value = parts[1];
            map.put(key, value);
        } else {
            System.out.println("ignoring line: " + line);
        }
    }
    reader.close();
    return map;
    }
    private static void copyAndRemoveFile(String pathFile,String destimationFolder, String fileName) {
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            String currentDate = DateUtil.getStringDate(new Date());
            String copyToDirectory = destimationFolder + "/store/" + currentDate;
            File srcFile = new File(pathFile+'/' + fileName);
            File directory = new File(copyToDirectory);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File desFile = new File(copyToDirectory + "/" + fileName);
            if (!desFile.exists()) {
                desFile.createNewFile();
            }
            inStream = new FileInputStream(srcFile);
            outStream = new FileOutputStream(desFile);
            byte[] buffer = new byte[1024];
            int length;
            //copy the file content in bytes 
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            inStream.close();
            outStream.close();
            //delete the original file
            srcFile.delete();            
            //System.out.println("File is copied successful!");            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static List getListCurvesRates(String url,String username,String password) throws IOException, FileNotFoundException, SQLException {
        List<CurvesModel> curves = new ArrayList<>();
        String sql = "select b.Curves_ShortName,c.Tenor,case when b.Nature = 'S' then a.ParRate\n" +
                    " when b.Nature = 'Z' then a.ZeroRate\n" +
                    " else a.ParRate end as Value "
                  + " from kplus..CurvesRatesHist a, kplus..Curves b, kplus..CurvesRates c\n" +
                    " where a.Curves_Id = b.Curves_Id \n" +
                    " and a.PeriodId = c.PeriodId and a.Curves_Id = c.Curves_Id\n" +
                    " and DATEDIFF(dd,HistDate, GETDATE()) = 1\n order by b.Curves_ShortName";
        ResultSet rs ;
        Statement st ;
        try{
        Connection connection = DBConnection.getInstance().getCon(url, username, password);
        st = connection.createStatement();
        //System.out.println(sql);
        rs = st.executeQuery(sql);
        while(rs.next()) {
            CurvesModel model = new CurvesModel();
            model.setCurve_name(rs.getString(1));
            model.setTenor(rs.getString(2));
            model.setValue(rs.getDouble(3));
            curves.add(model);
        }
        }catch(Exception e){
            e.printStackTrace();
        }
        return curves;
    }
    private static List getListCurvesRatesNR(String url,String username,String password) throws IOException, FileNotFoundException, SQLException {
        List<CurvesModel> curves = new ArrayList<>();
        String sql = "select b.Curves_ShortName,c.Tenor,case when b.Nature = 'S' then a.ParRate\n" +
                    " when b.Nature = 'Z' then a.ZeroRate\n" +
                    " else a.ParRate end as Value " +
                    " from kplus..CurvesRatesNRHist a, kplus..Curves b, kplus..CurvesRatesNR c\n" +
                    " where a.Curves_Id = b.Curves_Id \n" +
                    " and a.CurvesTenorsId = c.CurvesTenorsId and a.Curves_Id = c.Curves_Id\n" +
                    " and b.Curves_Id not in (54374,55986,55987,59025,59557,59564,59568,59578,59584,60111,62764,65032,65033,65034,65035,65036,65037,65642,66207)"+
                    " and DATEDIFF(dd,HistDate, GETDATE()) = 1 order by b.Curves_ShortName";
        ResultSet rs = null;
        Statement st = null;
        try{
        Connection connection = DBConnection.getInstance().getCon(url, username, password);
        st = connection.createStatement();
        //System.out.println(sql);
        rs = st.executeQuery(sql);
        while(rs.next()) {
            CurvesModel model = new CurvesModel();
            model.setCurve_name(rs.getString(1));
            model.setTenor(rs.getString(2));
            model.setValue(rs.getDouble(3));
            curves.add(model);
        }
        }catch(Exception e){
            e.printStackTrace();
        }
        return curves;
    }
    
    public static  boolean getDuplicates(Set array, String tableName, String[] rowData) {
        switch(tableName){
            case "SpotQuotes":
                if(!array.add(rowData[3])) {
                    System.err.println("[ERROR] Exist duplicated record in SpotQuotes with value: " + rowData[3] + " - Abort import file! "); 
                    return false;
                }else{
                    return true;
                }
            case "FxSwapRevalPoints":
                if(!array.add(rowData[0])) {
                    System.err.println("[ERROR] Exist duplicated record in FxSwapRevalPoints with value: " + rowData[0]+  " - Abort import file! "); 
                    return false;
                }else{
                    return true;
                }
                case "BondsQuotes":
                if(!array.add(rowData[1])) {
                    System.err.println("[ERROR] Exist duplicated record in BondsQuotes with value: " + rowData[1]+  " - Abort import file! "); 
                    return false;
                }else{
                    return true;
                }
        }
        return true;
    }

}
