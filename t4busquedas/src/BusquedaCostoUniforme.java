import java.util.*;

public class BusquedaCostoUniforme {

    static class Nodo implements Comparable<Nodo> {
        String estado;
        Nodo padre;
        int costo;

        Nodo(String estado, Nodo padre, int costo) {
            this.estado = estado;
            this.padre = padre;
            this.costo = costo;
        }

        @Override
        public int compareTo(Nodo otro) {
            return Integer.compare(this.costo, otro.costo);
        }
    }

    public static void ucs(String inicio, String objetivo) {
        PriorityQueue<Nodo> pq = new PriorityQueue<>();
        Map<String, Integer> visitados = new HashMap<>();

        pq.add(new Nodo(inicio, null, 0));
        visitados.put(inicio, 0);

        while (!pq.isEmpty()) {
            Nodo actual = pq.poll();

            if (actual.estado.equals(objetivo)) {
                System.out.println("Solución encontrada con costo: " + actual.costo);
                imprimirCamino(actual);
                return;
            }

            for (String sucesor : App.obtenerSucesores(actual.estado)) {
                int movimientoCosto = calcularCosto(actual.estado, sucesor);
                int nuevoCosto = actual.costo + movimientoCosto;

                if (!visitados.containsKey(sucesor) || nuevoCosto < visitados.get(sucesor)) {
                    pq.add(new Nodo(sucesor, actual, nuevoCosto));
                    visitados.put(sucesor, nuevoCosto);
                }
            }
        }

        System.out.println("No se encontró solución.");
    }

    //Para que no sea igual a bfs con costos de 1 entre movimientos, agregamos reglas de costo, mover a esquinas vale uno, a bordes 2 y a centro 3.
    public static int calcularCosto(String estadoActual, String estadoSiguiente) {
        int indexEspacio = estadoActual.indexOf(' ');

        switch(indexEspacio) {
            case 0: case 2: case 6: case 8: return 1; // esquinas
            case 1: case 3: case 5: case 7: return 2; // bordes
            case 4: return 3; // centro
        }
        return 1;
    }

    public static void imprimirCamino(Nodo nodo) {
        List<String> camino = new ArrayList<>();
        while (nodo != null) {
            camino.add(nodo.estado);
            nodo = nodo.padre;
        }
        Collections.reverse(camino);

        int pasos = 0;
        for (String estado : camino) {
            System.out.println("Paso " + pasos++);
            App.imprimirFormato(estado);
        }
        System.out.println("Número total de movimientos: " + (pasos-1));
    }
}
