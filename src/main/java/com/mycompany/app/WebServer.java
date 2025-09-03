//Proyecto 5 y 6
//Alan José Alejandre Domínguez
//7CV2
/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package com.mycompany.app;
import networking.Aggregator;
import networking.Libro;
import networking.SerializationUtils;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.io.InputStream;  
import java.util.StringTokenizer;
import java.math.BigInteger;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.DeserializationFeature;   
import com.fasterxml.jackson.databind.ObjectMapper;             
import com.fasterxml.jackson.databind.PropertyNamingStrategy;   

public class WebServer {
   
    private static final String STATUS_ENDPOINT = "/status";
    private static final String HOME_PAGE_ENDPOINT = "/";
    private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/ui_assets/";
    private static final String ENDPOINT_PROCESS = "/procesar_datos";
    private static final String MONITOR_ENDPOINT = "/monitoreo"; 
    private Aggregator aggregator = new Aggregator();
    private final int port; 
    private HttpServer server; 
    private final ObjectMapper objectMapper;

    public WebServer(int port) {
        this.port = port;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT); 
        HttpContext taskContext = server.createContext(ENDPOINT_PROCESS);
        HttpContext homePageContext = server.createContext(HOME_PAGE_ENDPOINT);
        HttpContext monitorContext = server.createContext(MONITOR_ENDPOINT);
        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);
        homePageContext.setHandler(this::handleRequestForAsset);
	monitorContext.setHandler(this::handleMonitor);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleRequestForAsset(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        byte[] response;

        String asset = exchange.getRequestURI().getPath(); 

        if (asset.equals(HOME_PAGE_ENDPOINT)) { 
            response = readUiAsset(HOME_PAGE_UI_ASSETS_BASE_DIR + "index.html");
        } else {
            response = readUiAsset(asset); 
        }
        addContentType(asset, exchange);
        sendResponse(response, exchange);
    }

    private byte[] readUiAsset(String asset) throws IOException {
        InputStream assetStream = getClass().getResourceAsStream(asset);

        if (assetStream == null) {
            return new byte[]{};
        }
        return assetStream.readAllBytes(); 
    }

    private static void addContentType(String asset, HttpExchange exchange) {

        String contentType = "text/html";  
        if (asset.endsWith("js")) {
            contentType = "text/javascript";
        } else if (asset.endsWith("css")) {
            contentType = "text/css";
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { 
            exchange.close();
            return;
        }
        String WORKER_ADDRESS_1 = "http://10.182.0.3:80/propuesta3";
	String WORKER_ADDRESS_2 = "http://10.182.0.4:80/propuesta3";
	String WORKER_ADDRESS_3 = "http://10.182.0.5:80/propuesta3";
	String task1,task2,task3;
	List<String> libros = new ArrayList<>();
	List<Double> ft_ift = new ArrayList<>();
	String division[];
	System.out.println("Headers de la solicitud:");
    Headers requestHeaders = exchange.getRequestHeaders();
    for (String headerName : requestHeaders.keySet()) {
        System.out.println(headerName + ": " + requestHeaders.getFirst(headerName));
    }
        try {
        
            FrontendSearchRequest frontendSearchRequest = objectMapper.readValue(exchange.getRequestBody().readAllBytes(), FrontendSearchRequest.class); 
            String busqueda = frontendSearchRequest.getSearchQuery();
		task1 = busqueda;
		task2 = busqueda;
		task3 = busqueda;
            System.out.println("Busqueda recibida: "+busqueda);
            Libro[] results = aggregator.sendTasksToWorkers(Arrays.asList(WORKER_ADDRESS_1,WORKER_ADDRESS_2,WORKER_ADDRESS_3), Arrays.asList(task1,task2,task3));
            for(int i=0; i < results.length; i++){
            	String []names = results[i].getA();
            	List<Double> n_r = results[i].getB();
            	for(int j=0; j<n_r.size(); j++){
            		libros.add(names[j]);
            		ft_ift.add(n_r.get(j));
            	} 
            }
            System.out.println("LIbros recibidos: "+libros);
            System.out.println("Factores:"+ft_ift);
            ordenarlibros(libros,ft_ift);
            FrontendSearchResponse frontendSearchResponse = new FrontendSearchResponse(libros);
            byte[] responseBytes = objectMapper.writeValueAsBytes(frontendSearchResponse);
            sendResponse(responseBytes, exchange);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage.getBytes(), exchange);
    }
    private void handleMonitor(HttpExchange exchange) throws IOException {
            OperatingSystemMXBean ob = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpu = ob.getSystemCpuLoad();
            long mem_u= ob.getTotalPhysicalMemorySize() - ob.getFreePhysicalMemorySize();
            double mem = ((double)mem_u) / ob.getTotalPhysicalMemorySize();
            String response = "1,"+cpu+","+mem;
            sendResponse(response.getBytes(), exchange);
    }
    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }
    private static void ordenarlibros(List<String> libros, List<Double> ft_ift){
    	for(int i=0; i<libros.size();i++){
    		for(int j=0; j<libros.size()-i-1; j++){
    			if(ft_ift.get(j) < ft_ift.get(j+1)){
    				double temp = ft_ift.get(j);
    				String temp2 = libros.get(j);
    				ft_ift.set(j,ft_ift.get(j+1));
    				libros.set(j, libros.get(j+1));
    				ft_ift.set(j+1,temp);
    				libros.set(j+1, temp2);
    			}
    		}
    	}
    }
}


