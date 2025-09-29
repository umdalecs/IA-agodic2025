import java.util.*;

public class BusquedaLimitada {
        static boolean encontrado = false;

    public static void dls(String inicio, String objetivo, int limite) {
        Map<String, String> padres = new HashMap<>();
        Set<String> visitados = new HashSet<>();

        System.out.println("Ejecutando búsqueda limitada con profundidad máxima = " + limite);
        encontrado = false;

        dlsRecursivo(inicio, objetivo, limite, padres, visitados);

        if (encontrado) {
            System.out.println("Solución encontrada.");
            App.imprimirCamino(padres, objetivo);
        } else {
            System.out.println("No se encontró solución hasta la profundidad " + limite);
        }
    }

    private static void dlsRecursivo(String actual, String objetivo, int limite,
                                     Map<String, String> padres, Set<String> visitados) {
        if (limite < 0 || encontrado) return;

        visitados.add(actual);

        if (actual.equals(objetivo)) {
            encontrado = true;
            return;
        }

        if (limite == 0) return; // límite alcanzado, no seguir

        for (String sucesor : App.obtenerSucesores(actual)) {
            if (!visitados.contains(sucesor)) {
                padres.put(sucesor, actual);
                dlsRecursivo(sucesor, objetivo, limite - 1, padres, visitados);
            }
        }
    }
}
