package com.polly.example;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

/* 實作FHIR搜尋，以family name作為搜尋參數 */
public class FHIRTestSearch {

	public static void main(String[] args) throws Exception{
	
		CamelContext context = new DefaultCamelContext();
		
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
						System.out.println("Identifier : "+ p.getIdentifierFirstRep().getValue());
						
					}
					
				});

			}
		});	
		
		context.start();
		Thread.sleep(10000);
		context.stop();
		
	}

}