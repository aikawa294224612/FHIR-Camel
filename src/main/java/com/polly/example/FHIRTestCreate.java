package com.polly.example;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

/* 實作FHIR創建資料(POST)，資料為寫死假資料 */
public class FHIRTestCreate {

	public static void main(String[] args) throws Exception{
	
		CamelContext context = new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder(){
			
			public void configure(){
				
				from("timer:mytimer?repeatCount=1")
				.setHeader("Content-Type", constant("application/fhir+json"))
				.process(new Processor() {

					public void process(Exchange exchange) throws Exception {
						
				        Patient patient = new Patient();
				        Identifier iden =new Identifier();
				        
				        iden.setValue("A00000000000000000000000");
				        iden.setSystem("http://www.moi.gov.tw/");
				        
				        List<Identifier> idens = new ArrayList<Identifier>();
				        idens.add(iden);
				        
				        patient.getNameFirstRep().setFamily("Polly");
				        patient.getNameFirstRep().setText("Polly Peng");
				        patient.setIdentifier(idens);
				        exchange.getIn().setBody(patient); 
						
					}
					
				})
				.marshal().fhirJson(true) 
				.to("file:src/test?fhirtestdb");
//				.to("fhir://create/resource?"
//			    		+ "inBody=resource&"
//			    		+ "fhirVersion=R4&"
//			    		+ "serverUrl=https%3A%2F%2Fhapi.fhir.org%2FbaseR4");
			}
		});	
		
		context.start();
		Thread.sleep(10000);
		context.stop();
		
	}

}