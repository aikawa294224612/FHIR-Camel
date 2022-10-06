package com.polly.example;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.hl7.fhir.r4.model.Patient;

/* 實作FHIR讀取資料，以ID搜尋 */
public class FHIRTestReadbyID {
	
	static PrivateData privatedata = new PrivateData();
	static String fhirserver = privatedata.fhirserverurl;

	public static void main(String[] args) throws Exception{
		
			CamelContext context = new DefaultCamelContext();
			
			context.addRoutes(new RouteBuilder(){
				
				public void configure(){
					
					from("timer:mytimer?repeatCount=1")
					.to("fhir://read/resourceById?"
							+ "serverUrl="+ fhirserver +"&"
							+ "resourceClass=Patient&"
							+ "longId=6978066&"
							+ "prettyPrint=true&"
							+ "encoding=JSON")
					.log("Success!")
					.process(new Processor() {
	
						public void process(Exchange exchange) throws Exception {
							
							Patient body = (Patient) exchange.getIn().getBody();
							String headers = exchange.getIn().getHeaders().toString();
							
							System.out.println(headers);
							System.out.println(body.getName().get(0).getFamily());
							
						}
						
					});
	
				}
			});	
			
			context.start();
			Thread.sleep(10000);
			context.stop();
			
		}

}
