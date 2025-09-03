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
package networking;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Aggregator {
    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient(); 
    }
    public Libro[] sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
        CompletableFuture<byte[]>[] futures = new CompletableFuture[workersAddresses.size()]; 
        for (int i = 0; i < workersAddresses.size(); i++) { 
            String workerAddress = workersAddresses.get(i); 
            String task = tasks.get(i);
            byte[] requestPayload = task.getBytes(); 
            futures[i] = webClient.sendTask(workerAddress, requestPayload); 
        }
	Libro [] respuesta = new Libro[workersAddresses.size()];
        for (int i = 0; i < workersAddresses.size(); i++) {
            respuesta[i]=(Libro)SerializationUtils.deserialize(futures[i].join());
        }

        return respuesta;
    }
}
