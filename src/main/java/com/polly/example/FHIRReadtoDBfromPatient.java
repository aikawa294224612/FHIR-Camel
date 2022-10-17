package com.polly.example;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	static Functions func = new Functions();


	public static void main(String[] args) throws Exception{
	
		String dbURL = "jdbc:sqlserver://"+ serverName +"\\"+ instanceName +":"+ port +";"
	    		+ "encrypt=true;databaseName=" + dbname + ";"
	    		+ "trustServerCertificate=true";
		
	    DataSource dataSource = func.setupDataSource(dbURL);
	    DefaultRegistry reg = new DefaultRegistry();
	    reg.bind("myDataSource", dataSource);
	        
	    CamelContext context = new DefaultCamelContext(reg);
		
		context.addRoutes(new RouteBuilder(){
			
			public void configure(){
				
				String id = "C102938475";
				
				from("timer:mytimer?repeatCount=1")
				.to("fhir://search/searchByUrl?"
						+ "serverUrl=https%3A%2F%2Fhapi.fhir.org%2FbaseR4%2F&"
						+ "url=https%3A%2F%2Fhapi.fhir.org%2FbaseR4%2FPatient%3Fidentifier%3D"+ id +"&"
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
						
						String uid = p.hasIdentifier() ? func.getID(p.getIdentifier(), codedata.idenCode) : "Unknow";  //身分證
						String pid = p.hasIdentifier() ? func.getID(p.getIdentifier(), codedata.recordCode) : "Unknow";  //病歷號
						String name = p.hasName() ? p.getName().get(0).getText() : "Unknow";  //姓名
						String gender = p.hasGender() ? func.getGender(p.getGender()): "0";  //性別
						String strDate = p.hasBirthDate() ? func.getBirth(p.getBirthDate()) : "Unknown";  //出生年月日					
						
						String address = p.hasAddress() ? p.getAddress().get(0).getText() : null;	//地址
						String phone = p.hasTelecom() ? func.getPhone(p.getTelecom(), ContactPoint.ContactPointUse.MOBILE) : null; //手機
						String marrage = p.hasMaritalStatus() ? func.getMarrage(p.getMaritalStatus()) : null;  //婚姻狀況
						
						String contactName = p.hasContact() ? p.getContact().get(0).getName().getText() : null;
						String contactAddress = p.hasContact() ? p.getContact().get(0).getAddress().getText() : null;
						String contactPhone = p.hasContact() ? func.getContectPhone(p.getContact().get(0), ContactPoint.ContactPointUse.MOBILE) : null;
						String contactRelationship = p.hasContact() ? func.getContactRelation(p.getContact().get(0)) : null;						
						String fid = p.getIdElement().getIdPart();
								
						String sql = "INSERT INTO "+ table +"([Id],[Pid],[Uid],[Name],[Sex],[Birthday],"
								+ "[Address],[Tel],[Marriage],[EmergencyContactTitle],[EmergencyContactName],"
								+ "[EmergencyContactAddress],[EmergencyContactTel],[RecordCreateTime],[RecordTransformTime],[Fid])"
								+ "VALUES('"+ pid +"','"+ pid +"','"+ uid +"','"+ name +"','"+ gender +"','"+ strDate +"',"
								+ "'"+ address +"','"+ phone +"','"+ marrage +"','"+ contactRelationship +"','"+ contactName +"','"+ contactAddress +"',"
								+ "'"+ contactPhone +"',GETDATE(),GETDATE(),'"+ fid +"')";
						
						exchange.getIn().setBody(sql);						
					}				
				})
				.to("jdbc:myDataSource")
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

}