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
import com.sun.net.httpserver.Headers;//Librerías que nos ayudan a construir un servidor http en java
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.management.OperatingSystemMXBean;
import networking.Aggregator;
import networking.Libro;
import networking.SerializationUtils;
import java.lang.management.ManagementFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class Servicio1 {
    private static final String TASK_ENDPOINT = "/propuesta3";  
    private static final String MONITOR_ENDPOINT = "/monitoreo"; 
    private final int port; 
    private HttpServer server;     

    public static void main(String[] args) {
        int serverPort = 80;  
        if (args.length == 1) { 
            serverPort = Integer.parseInt(args[0]);
        }
        Servicio1 webServer = new Servicio1(serverPort);
        webServer.startServer();
        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }
    public Servicio1 (int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        HttpContext taskContext = server.createContext(TASK_ENDPOINT);
        HttpContext monitorContext = server.createContext(MONITOR_ENDPOINT);
        taskContext.setHandler(this::handleTaskRequest);
	monitorContext.setHandler(this::handleMonitor);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }
    
    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        
        Headers headers = exchange.getRequestHeaders();    
        String frase = new String(exchange.getRequestBody().readAllBytes());
        String frase_div[] = frase.split("\\s+");
		List <Integer> n_ppalabra = new ArrayList<>();
		List <Float> ft = new ArrayList<>();
		List <Integer> nt = new ArrayList<>();
		List <Double> idf = new ArrayList<>();
		List <Double> tf_idf = new ArrayList<>();
		
		byte[] responseBytes;
		int contador = 1;
		int N,contador2;
		File biblioteca = new File("LIBROS_TXT1");
		if(biblioteca.exists()){
			String all_libros [] = biblioteca.list();
			for(String name: all_libros){
			System.out.println(name);
			List<String> libro_dividido = leerArchivo("LIBROS_TXT1/"+name);
			N = libro_dividido.size();
			System.out.println("NUmero total de palabras: "+N);
			calcNPalabras(n_ppalabra, frase_div,libro_dividido);
			calcFt(ft,n_ppalabra,N,contador,frase_div);
			contador++;
			System.out.println("\n");
			}
			ocurrenciaplibro(nt,n_ppalabra,frase_div);
			calculoidf(idf,nt,all_libros);
			calculotf_idf(tf_idf,ft,idf,frase_div);
			Libro lib = new Libro(all_libros,tf_idf);
			woextension(all_libros);
			System.out.println("Libros "+Arrays.toString(all_libros));
			System.out.println("TF_IDF"+tf_idf);
			responseBytes = SerializationUtils.serialize(lib);
		}
		else{
			responseBytes = "0,0".getBytes();
		}
        sendResponse(responseBytes, exchange);
    }
    private void handleMonitor(HttpExchange exchange) throws IOException {
            OperatingSystemMXBean ob = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpu = ob.getSystemCpuLoad();
            long mem_u= ob.getTotalPhysicalMemorySize() - ob.getFreePhysicalMemorySize();
            double mem = ((double)mem_u) / ob.getTotalPhysicalMemorySize();
            String response = "2,"+cpu+","+mem;
            sendResponse(response.getBytes(), exchange);
    }
    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }
    private static List<String> leerArchivo(String archivo){
		List<String> libro = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(archivo))){
			String linea;
			while((linea = br.readLine())!= null){
				String[] division = linea.split("\\s+");
				
				for (String a : division){
				if(!a.isEmpty()){
					libro.add(a);
					}
			}
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return libro;
	}
    private static void calcNPalabras(List <Integer> n_ppalabra, String frase_div[], List<String> libro_dividido){
	int contador_palabra;
			for(String compar : frase_div){
			System.out.println(compar);
			contador_palabra=0;
				for(String compar2: libro_dividido){
					if(compar2.toLowerCase().contains(compar.toLowerCase())){
						contador_palabra++;
					}
				}
				n_ppalabra.add(contador_palabra);
				System.out.println(n_ppalabra);
			}
	}
	private static void calcFt(List <Float> ft, List <Integer> n_ppalabra, int N, int c,String frase_div[]){
		float ftt,n_d;
		int ocurrencia;
		n_d = (float) N;
		int f = frase_div.length;
		for(int i=(c-1)*f; i<n_ppalabra.size(); i++){
			ocurrencia = n_ppalabra.get(i);
			ftt = ocurrencia / n_d;
			//System.out.println("Ocurrencia: "+ocurrencia+"N"+N+"FT:"+ftt);
			ft.add(ftt);
		}
	}
	private static void ocurrenciaplibro(List <Integer> nt, List <Integer> n_ppalabra, String frase_div[]){
		int frasetamano = frase_div.length;
			int tot,contador=0;
			for(int i=0; i< frasetamano ; i++)
				nt.add(0);
			
				for(int i=0; i<n_ppalabra.size(); i ++){
					if(contador == frasetamano)
						contador = 0;
					if(n_ppalabra.get(i)!=0){
						tot = nt.get(contador) + 1;
						nt.set(contador,tot);	
					}
					contador ++;
				}
	}
	private static void calculoidf(List<Double> idf, List<Integer>nt,String all_libros[]){
		int N = all_libros.length;
		for(int e : nt){
			if(e==0)
				idf.add(Math.log10(N/0.01));
			else
				idf.add(Math.log10(((double)N/e)));
		}
	}
	private static void calculotf_idf(List <Double> tf_idf,List <Float> ft, List <Double> idf,String frase_div[]){
		int n = frase_div.length;
		int n2 = ft.size() / n;
		double calculo;
		int conter ;
		for(int i=0; i<n2; i++){
			calculo = 0;
			conter = 0;
			for(int j = 0; j<n; j++){
				calculo = calculo + ft.get(n*i+conter)*idf.get(j);
				conter ++;
			}
			tf_idf.add(calculo);
		}
	}
	private static void woextension(String[] all_libros){
	int index_ext;
		for(int i=0; i<all_libros.length ; i++){
			index_ext = all_libros[i].lastIndexOf(".");
			all_libros[i] = all_libros[i].substring(0,index_ext)+".jpg";
		}	
	}
}
