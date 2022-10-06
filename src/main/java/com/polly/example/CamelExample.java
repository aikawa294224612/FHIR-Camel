package com.polly.example;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.hl7.fhir.r4.model.Patient;

public class CamelExample {

	public static void main(String[] args) throws Exception{
	
		CamelContext context = new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder(){
			
			public void configure(){
				// Define routing rules here:
				// copy files from outbox to inbox
				from("file:src/outbox?noop=true")
				.to("file:src/inbox")
				;

			}
		});	
		
		while(true) {
			context.start();
		}
		//context.start();
		
	}

}