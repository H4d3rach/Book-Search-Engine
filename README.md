# Book-Search-Engine (Project 5 & 6 Distributed Systems)
This project is a **distributed book search engine** implemented in **Java with Maven**.  
It consists of a **main server** that distributes search queries to **three worker nodes** (deployed on Google Cloud Platform), aggregates the results using **TF-IDF**, and returns them to the client in ranked order.

---

## Features

- **Main Web Server**
  - Built with `com.sun.net.httpserver.HttpServer`.  
  - Handles search queries and distributes them to 3 worker nodes.  
  - Aggregates results, sorts them by **TF-IDF relevance**, and returns JSON to the frontend.  
  - Provides system monitoring (CPU, memory).  

- **Worker Nodes**
  - Each worker runs an HTTP server.  
  - Endpoints:
    - `/propuesta3` → Receives a search query and computes TF-IDF scores for the text files in **LIBROS_TXT#**.  
    - `/monitoreo` → Returns CPU and memory usage of the worker.  
  - Processes plain text books, splits words, and applies TF, IDF, and TF-IDF calculations.  
  - Returns a serialized `Libro` object containing:
    - The list of book filenames (converted to `.jpg` references).  
    - Their corresponding TF-IDF scores.  

- **Monitoring**
  - Both server and workers expose endpoints with CPU and memory usage.  

- **Tech stack**
  - Java.  
  - JSON parsing with **Jackson**.  
  - Project structure managed with **Maven**.  

---

## System Architecture
[ Client / Browser ]
│
▼
[ Main Server ]
- /status
- /procesar_datos
- /monitoreo
│
├──> [ Worker 1 @ GCP ] /propuesta3 , /monitoreo
├──> [ Worker 2 @ GCP ] /propuesta3 , /monitoreo
└──> [ Worker 3 @ GCP ] /propuesta3 , /monitoreo


1. The client sends a search query (`/procesar_datos`).  
2. The main server distributes the query to 3 workers.  
3. Each worker loads its local **LIBROS_TXT** folder, calculates TF-IDF, and sends results back.  
4. The server aggregates, sorts by relevance, and returns a JSON response to the client.  

---

## Endpoints

### Main Server
| Endpoint          | Method | Description |
|-------------------|--------|-------------|
| `/`               | GET    | Homepage + static assets |
| `/status`         | GET    | Health check ("Server is alive") |
| `/procesar_datos` | POST   | Distributes a search query to workers and returns ranked results |
| `/monitoreo`      | GET    | Returns CPU and memory usage (`1,cpu,mem`) |

### Worker Nodes
| Endpoint       | Method | Description |
|----------------|--------|-------------|
| `/propuesta3`  | POST   | Accepts a search query and computes TF-IDF from local books |
| `/monitoreo`   | GET    | Returns CPU and memory usage (`2,cpu,mem`) |

---

## How to Run

### Clone and build the project
```bash
git clone <this-repo>
cd my-app
mvn clean install
cd target
java -cp "my-app-1.0-SNAPSHOT.jar:libs/*" com.mycompany.app.App 8080
