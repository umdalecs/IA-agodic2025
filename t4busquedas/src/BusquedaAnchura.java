import java.util.*;

public class BusquedaAnchura {

    public static void bfs(String inicio, String objetivo){
        Queue<String> cola = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        Map<String, String> padres = new HashMap<>();

        cola.add(inicio);
        visitados.add(inicio);
        padres.put(inicio, null);
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            if (actual.equals(objetivo)) {
                System.out.println("Solución encontrada.");
                App.imprimirCamino(padres, actual);
                return;
            }
            for (String sucesor : App.obtenerSucesores(actual)) {
                if (!visitados.contains(sucesor)) {
                    cola.add(sucesor);
                    visitados.add(sucesor);
                    padres.put(sucesor, actual);
                }
            }
        }
        System.out.println("No se encontró solución.");
    }
}
