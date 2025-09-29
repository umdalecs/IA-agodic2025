import java.util.*;

public class BusquedaHeuristica {
    
    // Algoritmo A* (f = g + h)
    public static void aStar(String inicio, String objetivo) {
        PriorityQueue<Nodo> frontera = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<String, String> padres = new HashMap<>();
        Map<String, Integer> gCost = new HashMap<>();
        Set<String> visitados = new HashSet<>();

        gCost.put(inicio, 0);
        frontera.add(new Nodo(inicio, 0, heuristicaManhattan(inicio, objetivo)));
        padres.put(inicio, null);

        while (!frontera.isEmpty()) {
            Nodo actual = frontera.poll();
            String estado = actual.estado;

            if (estado.equals(objetivo)) {
                System.out.println("Solución encontrada con A*.");
                App.imprimirCamino(padres, estado);
                return;
            }

            if (visitados.contains(estado)) continue;
            visitados.add(estado);

            for (String sucesor : App.obtenerSucesores(estado)) {
                int nuevoG = gCost.get(estado) + 1; // costo de movimiento = 1
                if (!gCost.containsKey(sucesor) || nuevoG < gCost.get(sucesor)) {
                    gCost.put(sucesor, nuevoG);
                    int h = heuristicaManhattan(sucesor, objetivo);
                    frontera.add(new Nodo(sucesor, nuevoG, h));
                    padres.put(sucesor, estado);
                }
            }
        }

        System.out.println("No se encontró solución con A*.");
    }

    // Heurística 1: número de fichas mal colocadas
    public static int heuristicaMisplaced(String estado, String objetivo) {
        int count = 0;
        for (int i = 0; i < estado.length(); i++) {
            if (estado.charAt(i) != ' ' && estado.charAt(i) != objetivo.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    // Heurística 2: distancia Manhattan
    public static int heuristicaManhattan(String estado, String objetivo) {
        int distancia = 0;
        for (int i = 0; i < estado.length(); i++) {
            char c = estado.charAt(i);
            if (c != ' ') {
                int posObjetivo = objetivo.indexOf(c);
                int filaActual = i / 3, colActual = i % 3;
                int filaObj = posObjetivo / 3, colObj = posObjetivo % 3;
                distancia += Math.abs(filaActual - filaObj) + Math.abs(colActual - colObj);
            }
        }
        return distancia;
    }

    static class Nodo {
        String estado;
        int g, h, f;
        Nodo(String estado, int g, int h) {
            this.estado = estado;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }
}
