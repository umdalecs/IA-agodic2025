import java.util.*;

public class BusquedaProfundidad {
    
    public static void dfs(String inicio, String objetivo){
        Stack<String> pila = new Stack<>();
        Set<String> visitados = new HashSet<>();
        Map<String, String> padres = new HashMap<>();

        pila.push(inicio);
        visitados.add(inicio);
        padres.put(inicio, null);

        while (!pila.isEmpty()) {
            String actual = pila.pop();
            if (actual.equals(objetivo)) {
                System.out.println("Solución encontrada.");
                App.imprimirCamino(padres, actual);
                return;
            }
            for (String sucesor : App.obtenerSucesores(actual)) {
                if (!visitados.contains(sucesor)) {
                    pila.push(sucesor);
                    visitados.add(sucesor);
                    padres.put(sucesor, actual);
                }
            }
        }
        System.out.println("No se encontró solución.");
    }
}
