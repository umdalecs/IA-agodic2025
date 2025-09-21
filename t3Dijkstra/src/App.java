import java.util.*;

class Edge {
    int destino;
    int peso;

    public Edge(int destino, int peso) {
        this.destino = destino;
        this.peso = peso;
    }
}

class Graph {
    private final int vertices;
    private final List<List<Edge>> adjList;

    public Graph(int vertices) {
        this.vertices = vertices;
        adjList = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            adjList.add(new ArrayList<>());
        }
    }

    public void addEdge(int origen, int destino, int peso) {
        adjList.get(origen).add(new Edge(destino, peso));
        adjList.get(destino).add(new Edge(origen, peso)); // Si es grafo no dirigido
    }

    public void dijkstra(int inicio) {
        // Distancias inicializadas en infinito
        int[] distancias = new int[vertices];
        Arrays.fill(distancias, Integer.MAX_VALUE);
        distancias[inicio] = 0;

        // Cola de prioridad para elegir el nodo con menor distancia
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.add(new int[]{inicio, 0});

        // Array para reconstruir caminos
        int[] predecesores = new int[vertices];
        Arrays.fill(predecesores, -1);

        boolean[] visitado = new boolean[vertices];

        while (!pq.isEmpty()) { 
            int[] actual = pq.poll();
            int nodo = actual[0];
            int distancia = actual[1];

            if (visitado[nodo]) continue;
            visitado[nodo] = true;

            for (Edge edge : adjList.get(nodo)) {
                int vecino = edge.destino;
                int nuevaDistancia = distancia + edge.peso;

                if (nuevaDistancia < distancias[vecino]) {
                    distancias[vecino] = nuevaDistancia;
                    predecesores[vecino] = nodo;
                    pq.add(new int[]{vecino, nuevaDistancia});
                }
            }
        }

        // Mostrar resultados
        System.out.println("Distancias mínimas desde el nodo " + inicio + ":");
        for (int i = 0; i < vertices; i++) {
            System.out.print("Nodo " + i + " → Distancia: " + distancias[i]);
            if (distancias[i] != Integer.MAX_VALUE) {
                System.out.print(" | Camino: ");
                imprimirCamino(predecesores, i);
            }
            System.out.println();
        }
    }

    private void imprimirCamino(int[] predecesores, int nodo) {
        List<Integer> camino = new ArrayList<>();
        while (nodo != -1) {
            camino.add(nodo);
            nodo = predecesores[nodo];
        }
        Collections.reverse(camino);
        for (int i = 0; i < camino.size(); i++) {
            System.out.print(camino.get(i));
            if (i < camino.size() - 1) System.out.print(" -> ");
        }
    }
}

public class App {
    public static void main(String[] args) {
        Graph grafo = new Graph(5);

        // Agregar aristas: (origen, destino, peso)
        grafo.addEdge(0, 1, 4);
        grafo.addEdge(0, 2, 1);
        grafo.addEdge(2, 1, 2);
        grafo.addEdge(1, 3, 1);
        grafo.addEdge(2, 3, 5);
        grafo.addEdge(3, 4, 3);

        // Ejecutar Dijkstra desde el nodo 0
        grafo.dijkstra(0);
    }
}
