package com.polly.example;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultRegistry;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;

/* 實作FHIR搜尋，以family name作為搜尋參數 */
public class FHIRReadtoDBfromPatient {
	
	static PrivateData privateData = new PrivateData();
	static String serverName = privateData.serverName;
	static String instanceName = privateData.instanceName;
	static String port = privateData.port;
	static String dbname = privateData.dbname;
	static String username = privateData.username;
	static String pw = privateData.pw;
	static String table = privateData.table;
	static String fhirserver = privateData.fhirserverurl;
	static String className = privateData.className;
	
	static FHIRData fhirdata = new FHIRData();
	static CodeData codedata = new CodeData();

	public static void main(String[] args) throws Exception{
	
		String dbURL = "jdbc:sqlserver://"+ serverName +"\\"+ instanceName +":"+ port +";"
	    		+ "encrypt=true;databaseName=" + dbname + ";"
	    		+ "trustServerCertificate=true";
		
	    DataSource dataSource = setupDataSource(dbURL);
	    DefaultRegistry reg = new DefaultRegistry();
	    reg.bind("myDataSource", dataSource);
	        
	    CamelContext context = new DefaultCamelContext(reg);
		
		context.addRoutes(new RouteBuilder(){
			
			public void configure(){
				
				from("timer:mytimer?repeatCount=1")
				.to("fhir://search/searchByUrl?"
						+ "serverUrl=https%3A%2F%2Fhapi.fhir.org%2FbaseR4%2F&"
						+ "url=https%3A%2F%2Fhapi.fhir.org%2FbaseR4%2FPatient%3Fidentifier%3DH123450789&"
						+ "prettyPrint=true&"
						+ "encoding=JSON")
				.process(new Processor() {

					public void process(Exchange exchange) throws Exception {
						
						Bundle body = (Bundle) exchange.getIn().getBody();
						String header = exchange.getIn().getHeaders().toString();
						
						System.out.println(header);
						System.out.println(body);
						System.out.println("Total: " + body.getTotal());
						System.out.println(body.getEntry().get(0).getResource());
						
						Patient p = (Patient) body.getEntry().get(0).getResource();												
						
						String uid = getID(p.getIdentifier(), codedata.idenCode) == "" ? "Unknow" : getID(p.getIdentifier(), codedata.idenCode);  //身分證
						String pid = getID(p.getIdentifier(), codedata.recordCode)  == "" ? "Unknow" : getID(p.getIdentifier(), codedata.recordCode);  //病歷號
						String name = p.getName().get(0).getText() == "" ? "Unknow" : p.getName().get(0).getText();  //姓名
						String gender = getGender(p.getGender());  //性別
						
						//出生年月日
						DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");  
						String strDate = dateFormat.format(p.getBirthDate());  
						
						String address = p.getAddress().get(0).getText() == "" ? "Unknow" : p.getAddress().get(0).getText();	//地址
						String phone = getPhone(p.getTelecom(), ContactPoint.ContactPointUse.MOBILE) == "" ? "Unknow" : getPhone(p.getTelecom(), ContactPoint.ContactPointUse.MOBILE); //手機
						String marrage = getMarrage(p.getMaritalStatus());  //婚姻狀況
						
						String contactName = p.getContact().size() > 0 ? p.getContact().get(0).getName().getText() : "Unknown";
						String contactAddress = p.getContact().size() > 0 ? p.getContact().get(0).getAddress().getText() : "Unknown";
						String contactPhone = p.getContact().size() > 0 ? getPhone(p.getContact().get(0).getTelecom(), ContactPoint.ContactPointUse.MOBILE) : "Unknown";
						String contactRelationship = p.getContact().size() > 0 ? getContactRelation(p.getContact().get(0).getRelationship().get(0)) : "Unknown";						
						
						String sql = "INSERT INTO [dbo].[Patient]([Id],[Pid],[Uid],[Name],[Sex],[Birthday],"
								+ "[Address],[Tel],[Marriage],[EmergencyContactTitle],[EmergencyContactName],"
								+ "[EmergencyContactAddress],[EmergencyContactTel],[RecordCreateTime],[RecordTransformTime])"
								+ "VALUES('"+ pid +"','"+ pid +"','"+ uid +"','"+ name +"','"+ gender +"','"+ strDate +"',"
								+ "'"+ address +"','"+ phone +"','"+ marrage +"','"+ contactRelationship +"','"+ contactName +"','"+ contactAddress +"',"
								+ "'"+ contactPhone +"',GETDATE(),GETDATE())";
						
						System.out.println(sql);
						
						exchange.getIn().setBody(sql);						
					}				
				})
				//.to("jdbc:myDataSource")
				.process(new Processor() {
					public void process(Exchange exchange) throws Exception {						
				        
						System.out.println("Created!");
						
					}
					
				});

			}
		});	
		
		context.start();
		Thread.sleep(10000);
		context.stop();
		
	}
	
	private static DataSource setupDataSource(String connectURI) {
	    BasicDataSource ds = new BasicDataSource();
	    ds.setDriverClassName(className);
	    ds.setUsername(username);
	    ds.setPassword(pw);
	    ds.setUrl(connectURI);
	    return ds;
	  }
	
	private static String getID(List<Identifier> idens, String code) {
		String iden = "";
		for(int i=0; i<idens.size(); i++) {
			if(idens.get(i).getType().getCoding().get(0).getCode().equals(code)) {
				iden = idens.get(i).getValue();
				break;
			}
		}
		
		return iden;

	  }
	
	private static String getGender(AdministrativeGender administrativeGender) {
		if(administrativeGender == AdministrativeGender.FEMALE) {
			return "2";
		}else if(administrativeGender == AdministrativeGender.MALE)  {
			return "1";
		}else {
			return "0";
		}


	  }
	
	private static String getPhone(List<ContactPoint> contacts, ContactPoint.ContactPointUse use) {
		String value = "";
		for(int i=0; i<contacts.size(); i++) {
			if(contacts.get(i).getUse() == use) {
				value = contacts.get(i).getValue();
				break;
			}
		}
		
		return value;

	  }
	
	private static String getMarrage(CodeableConcept code) {
		if(code.getCoding().get(0).getCode().equals(codedata.unmarried)) {
			return "未婚";
		}else if(code.getCoding().get(0).getCode().equals(codedata.married)){
			return "已婚";
		}else if(code.getCoding().get(0).getCode().equals(codedata.divorce)){
			return "離婚";
		}else {
			return "Unknown";
		}

	  }
	
	private static String getContactRelation(CodeableConcept code) {
		if(code.getCoding().get(0).getCode().equals(codedata.father)) {
			return "父";
		}else if(code.getCoding().get(0).getCode().equals(codedata.mother)){
			return "母";
		}else if(code.getCoding().get(0).getCode().equals(codedata.sprouse)){
			return "配偶";
		}else {
			return "Unknown";
		}

	  }

}