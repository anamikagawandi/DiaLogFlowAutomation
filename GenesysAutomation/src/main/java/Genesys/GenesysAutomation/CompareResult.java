package Genesys.GenesysAutomation;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentLoggerReporter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesys.context.response.Parameters;
import com.genesys.response.json.ResponseJson;

public class CompareResult {

	FileInputStream file;
	Workbook wb;
	Sheet sheet;
	Row row;
	Cell cell;
	Logger logger;
	Initializer init;
	FileOutputStream outputStream;
	File file_name;
	ResponseJson response_df;
	ChatScript chat;
	CMS cms;
	String[] df_json_response;
	String cms_json_response;
	ObjectMapper mapper;
	ExtentReports extent; 
	ExtentHtmlReporter reporter;
	ExtentTest test;
	
	public CompareResult()
	{
		logger = Logger.getLogger(this.getClass());
		init = new Initializer();
		chat=new ChatScript();
		cms=new CMS();
		
		mapper = new ObjectMapper();
		
		reporter = new ExtentHtmlReporter(init.prop.getProperty("reportPath"));
		extent = new ExtentReports();
		extent.attachReporter(reporter);
		extent.setAnalysisStrategy(AnalysisStrategy.TEST);
		reporter.loadXMLConfig(init.prop.getProperty("extentReportConfig"));
	}


	public Workbook loadFile()
	{
		String file_extn=init.prop.getProperty("excel_path").substring(init.prop.getProperty("excel_path").lastIndexOf("."));
		logger.info("File extension :"+file_extn);
		//System.out.println(file_extn);

		file_name=new File(init.prop.getProperty("excel_path"));

		try {
			//System.out.println("Before setting I/P Stream");
			file = new FileInputStream(file_name);			
			//System.out.println("After setting I/P Stream");
		}catch(FileNotFoundException e)
		{
			//System.out.println("Excel file not found");
			logger.error("Excel file not found",e);
		}catch(IOException e){
			//System.out.println("IOException occurred");
			logger.error("IOException occurred",e);
		}


		try {
			wb=WorkbookFactory.create(file);
		} catch (EncryptedDocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		/*if(file_extn.equalsIgnoreCase((".xlsx")))
		{
			//System.out.println("In if block of File extn xlxs");
			try {
				wb = new XSSFWorkbook(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//System.out.println("Problem in loading file"+e);
				logger.error("Problem in loading file",e);
			}
			sheet = (XSSFSheet) wb.getSheetAt(0);
		}
		else{
			try {
				wb = new HSSFWorkbook(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("Problem in loading file",e);
			}
			sheet = (HSSFSheet) wb.getSheetAt(0);
		}
		 */

		return wb;

	}

	
	public void compareData()
	{

		loadFile();
		sheet = wb.getSheetAt(0);


		int size=sheet.getLastRowNum();
		int col=sheet.getRow(0).getLastCellNum();
		System.out.println("row: "+size +"col: " +col);


		try
		{

			for(int i=1;i<=size;i++)
			{


				//System.out.println("This is indent"+sheet.getRow(i).getCell(2).getStringCellValue());
				sheet.getRow(i).getCell(5).setCellValue("");
				sheet.getRow(i).getCell(6).setCellValue("");
				sheet.getRow(i).getCell(7).setCellValue("");
				sheet.getRow(i).getCell(8).setCellValue("");
				sheet.getRow(i).getCell(9).setCellValue("");
				sheet.getRow(i).getCell(12).setCellValue("");
				sheet.getRow(i).getCell(13).setCellValue("");
				sheet.getRow(i).getCell(14).setCellValue("");
				sheet.getRow(i).getCell(15).setCellValue("");
				sheet.getRow(i).getCell(17).setCellValue("");
				sheet.getRow(i).getCell(18).setCellValue("");


				String question=sheet.getRow(i).getCell(1).getStringCellValue().trim();
				
			
				
				
				String cms_id = "NA";
				if(sheet.getRow(i).getCell(10).getNumericCellValue()>0)
					cms_id=Integer.toString((int) sheet.getRow(i).getCell(10).getNumericCellValue()).trim();
				System.out.println("CMS ID is :\t"+cms_id);
				String cms_answer=null;
				System.out.println(question);

				String ans="NA";
				String intent="NA";
				String sheet_intent="NA";
				String sheet_entity_att="NA",sheet_entity_value="NA",entity_att="NA",entity_value="NA";

				String sheet_context="NA", df_context="NA";

				if(question.isEmpty())
					break;

				test=extent.createTest(question);
				
				
				df_json_response=chat.getResponseString(question,
						init.prop.getProperty("api_uri").replace("{id}",
								Integer.toString((int) sheet.getRow(i).getCell(0).getNumericCellValue()).trim()));
				
				
				
				
				//System.out.println("This is th response"+  df_json_response );	
				response_df=chat.runChatScript(df_json_response[1]);

				intent=response_df.getQueryResult().getIntent().getDisplayName().trim();
				sheet_intent=sheet.getRow(i).getCell(2).getStringCellValue().trim();




				if(cms_id.trim().equalsIgnoreCase("NA"))
				{
					System.out.println("Inside CMS id null block");
					ArrayList<String> default_ans=new ArrayList<String>();
					//default_ans.add("I didn't get that. Can you say it again?");
					//default_ans.add("I missed what you said. What was that?");
					//default_ans.add("Sorry, could you say that again?");
					////default_ans.add("Sorry, can you say that again?");
					//default_ans.add("Can you say that again?");
					//default_ans.add("Sorry, I didn't get that. Can you rephrase?");
					//default_ans.add("Sorry, what was that?");
					//default_ans.add("One more time?");
					//default_ans.add("What was that?");
					//default_ans.add("Say that one more time?");
					////default_ans.add("I didn't get that. Can you repeat?");
					//default_ans.add("I missed that, say that again?");
					default_ans.add("Connect to live agent.");



					ans=response_df.getQueryResult().getFulfillmentText().trim();

					if(default_ans.contains(ans) && (intent.equalsIgnoreCase("Default Fallback Intent".trim()) || intent.equalsIgnoreCase("1.connectToLiveAgent".trim())))
					{
						sheet.getRow(i).getCell(11).setCellValue("PASS");
						test.log(Status.PASS,"");
						test.pass("DF JSON:\n"+mapper.writeValueAsString(response_df));
						
					}
					else
					{
						sheet.getRow(i).getCell(11).setCellValue("FAIL");
						test.log(Status.FAIL,"");
						test.fail("DF JSON:\n"+mapper.writeValueAsString(response_df));
					}

					
					//to add values of entities from df
					if(!response_df.getQueryResult().getParameters().getWhat().trim().isEmpty())
					{
						entity_att="what";
						entity_value=response_df.getQueryResult().getParameters().getWhat().trim();
					}
					else if(!response_df.getQueryResult().getParameters().getWhen().trim().isEmpty())
					{
						entity_att="when";
						entity_value=response_df.getQueryResult().getParameters().getWhen().trim();
					}
					else if(!response_df.getQueryResult().getParameters().getWhere().trim().isEmpty())
					{
						entity_att="where";
						entity_value=response_df.getQueryResult().getParameters().getWhere().trim();
					}
					else if(!response_df.getQueryResult().getParameters().getWho().trim().isEmpty())
					{
						entity_att="who";
						entity_value=response_df.getQueryResult().getParameters().getWho().trim();
					}
					else if(!response_df.getQueryResult().getParameters().getItem().trim().isEmpty())
					{
						entity_att="item";
						entity_value=response_df.getQueryResult().getParameters().getItem().trim();
					}
					else if(!response_df.getQueryResult().getParameters().getTopic().trim().isEmpty())
					{
						entity_att="topic";
						entity_value=response_df.getQueryResult().getParameters().getTopic().trim();
					}
					else if(!response_df.getQueryResult().getParameters().getDirections().trim().isEmpty())
					{
						entity_att="directions";
						entity_value=response_df.getQueryResult().getParameters().getDirections().trim();
					}
					else if(!response_df.getQueryResult().getParameters().getGeneral().trim().isEmpty())
					{
						entity_att="general";
						entity_value=response_df.getQueryResult().getParameters().getGeneral().trim();
					}
					
					
					
					sheet.getRow(i).getCell(7).setCellValue(entity_att);
					sheet.getRow(i).getCell(8).setCellValue(entity_value);
					sheet.getRow(i).getCell(5).setCellValue(ans);
					//sheet.getRow(i).getCell(2).setCellValue("Default Fallback Intent");
					
					System.out.println("Question asked:\t"+question);
					System.out.println("Answer by DF :\t"+ans);
					System.out.println("Intent expected:\t"+"Default Fallback Intent");
					System.out.println("Intent actual:\t"+intent);

				}
				else {
					System.out.println("Inside cms id block");
					try{

						cms_json_response=cms.getCMSResponseString(cms_id.trim());
						System.out.println(cms_json_response);
						try {
							cms_answer=cms.getCMSResponse(cms_json_response).getAnswer().trim();
							System.out.println(cms_answer);
						} catch (Exception e) {
							//e.printStackTrace();
							sheet.getRow(i).getCell(13).setCellValue(e.getMessage());
						}



						if(!response_df.getQueryResult().getFulfillmentText().trim().isEmpty())
						{
							ans=response_df.getQueryResult().getFulfillmentText().trim();
						}


						//to add values when sheet has entity values
						if(!sheet.getRow(i).getCell(3).getStringCellValue().isEmpty())
						{
							sheet_entity_att=sheet.getRow(i).getCell(3).getStringCellValue().trim();
							sheet_entity_value=sheet.getRow(i).getCell(4).getStringCellValue().trim();
						}

						if(!sheet.getRow(i).getCell(16).getStringCellValue().isEmpty())
						{
							sheet_context=sheet.getRow(i).getCell(16).getStringCellValue().trim();
							df_context=response_df.getQueryResult().getOutputContexts().get(0).getParameters().getEntity().trim();
						}


						//to add values of entities from df
						if(!response_df.getQueryResult().getParameters().getWhat().trim().isEmpty())
						{
							entity_att="what";
							entity_value=response_df.getQueryResult().getParameters().getWhat().trim();
						}
						else if(!response_df.getQueryResult().getParameters().getWhen().trim().isEmpty())
						{
							entity_att="when";
							entity_value=response_df.getQueryResult().getParameters().getWhen().trim();
						}
						else if(!response_df.getQueryResult().getParameters().getWhere().trim().isEmpty())
						{
							entity_att="where";
							entity_value=response_df.getQueryResult().getParameters().getWhere().trim();
						}
						else if(!response_df.getQueryResult().getParameters().getWho().trim().isEmpty())
						{
							entity_att="who";
							entity_value=response_df.getQueryResult().getParameters().getWho().trim();
						}
						else if(!response_df.getQueryResult().getParameters().getItem().trim().isEmpty())
						{
							entity_att="item";
							entity_value=response_df.getQueryResult().getParameters().getItem().trim();
						}
						else if(!response_df.getQueryResult().getParameters().getTopic().trim().isEmpty())
						{
							entity_att="topic";
							entity_value=response_df.getQueryResult().getParameters().getTopic().trim();
						}
						else if(!response_df.getQueryResult().getParameters().getDirections().trim().isEmpty())
						{
							entity_att="directions";
							entity_value=response_df.getQueryResult().getParameters().getDirections().trim();
						}
						else if(!response_df.getQueryResult().getParameters().getGeneral().trim().isEmpty())
						{
							entity_att="general";
							entity_value=response_df.getQueryResult().getParameters().getGeneral().trim();
						}
						


						try {
							if(cms_answer.trim().isEmpty())
								cms_answer="Blank answer from CMS";
						}
						catch(Exception e)
						{
							cms_answer="Error to fetch from CMS";
						}

						if(ans.equalsIgnoreCase(cms_answer) &&
								intent.equalsIgnoreCase(sheet_intent)
								&& entity_att.equalsIgnoreCase(sheet_entity_att)  && 
								entity_value.equalsIgnoreCase(sheet_entity_value) &&
								df_context.equalsIgnoreCase(sheet_context) )
						{
							sheet.getRow(i).getCell(11).setCellValue("PASS");
							test.log(Status.PASS, question);
							test.pass("DF JSON:\n"+mapper.writeValueAsString(response_df));
							test.pass("CMS JSON:\n"+mapper.writeValueAsString(cms_json_response));
						}
						else
						{
							sheet.getRow(i).getCell(11).setCellValue("FAIL");
							test.log(Status.FAIL, question);
							test.fail("DF JSON:\n"+mapper.writeValueAsString(response_df));
							test.fail("CMS JSON:\n"+mapper.writeValueAsString(cms_json_response));
						}
							


					}catch(Exception e)
					{
						sheet.getRow(i).getCell(11).setCellValue("Some error occured. Check Manually");
						e.printStackTrace();
					}
					
					
					
					System.out.println("Question asked:\t"+question);
					System.out.println("Answer by DF :\t"+ans);
					System.out.println("Answer by CMS :\t"+cms_answer);
					System.out.println("Intent expected:\t"+sheet.getRow(i).getCell(2).getStringCellValue().trim());
					System.out.println("Intent actual:\t"+intent);
					System.out.println("Entity Att expected :\t"+sheet_entity_att);
					System.out.println("Entity Att actual :\t"+entity_att);
					System.out.println("Entity Value expected :\t"+sheet_entity_value);
					System.out.println("Entity Value actual :\t"+entity_value);
					System.out.println("Context Value expected :\t"+sheet_context);
					System.out.println("Context Value actual :\t"+df_context);
					System.out.println("Intent Detection Confidence :\t"+response_df.getQueryResult().getIntentDetectionConfidence().doubleValue());
					
					sheet.getRow(i).getCell(7).setCellValue(entity_att);
					sheet.getRow(i).getCell(8).setCellValue(entity_value);
					sheet.getRow(i).getCell(13).setCellValue(cms_json_response);
					//sheet.getRow(i).getCell(2).setCellValue(sheet_intent.toString());
					
				}

				System.out.println("Intent Detection Confidence :\t"+response_df.getQueryResult().getIntentDetectionConfidence().doubleValue());
				
				sheet.getRow(i).getCell(5).setCellValue(cms_answer);
				sheet.getRow(i).getCell(6).setCellValue(intent);
				
				sheet.getRow(i).getCell(9).setCellValue(ans);
				if(df_json_response[1].length()<32767)
					sheet.getRow(i).getCell(12).setCellValue(df_json_response[1]);
				
				
				sheet.getRow(i).getCell(17).setCellValue(df_context);
				sheet.getRow(i).getCell(18).setCellValue(response_df.getQueryResult().getIntentDetectionConfidence().doubleValue());


				Date date = Calendar.getInstance().getTime();  
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd-hh:mm:ss");  

				String strDate = dateFormat.format(date);  

				sheet.getRow(i).getCell(14).setCellValue(strDate);
				sheet.getRow(i).getCell(15).setCellValue(df_json_response[0]);



			}

			
			//driver.quit();

		}catch(
				Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				file.close();
				outputStream = new FileOutputStream(file_name);
				wb.write(outputStream);
				extent.flush();

				System.out.println("Task Complete");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}


	}

}
