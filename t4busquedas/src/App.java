import java.util.*;

public class App {

    static String estadoInicial ="7245 6831";
    static String estadoFinal =" 12345678";
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("\n--- Menú de Búsquedas ---");
            System.out.println("1. Búsqueda Primero en Anchura (BFS)");
            System.out.println("2. Búsqueda Primero en Profundidad (DFS)");
            System.out.println("3. Búsqueda Costo Uniforme (UCS)");
            System.out.println("4. Búsqueda Limitada (DLS)");
            System.out.println("5. Búsqueda Heurística (A*)");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = sc.nextInt();

            switch (opcion) {
                case 1:
                    BusquedaAnchura.bfs(estadoInicial, estadoFinal);
                    break;
                case 2:
                    BusquedaProfundidad.dfs(estadoInicial, estadoFinal);
                    break;
                case 3:
                    BusquedaCostoUniforme.ucs(estadoInicial, estadoFinal);
                    break;
                case 4:
                    System.out.print("Ingrese el límite de profundidad: ");
                    int limite = sc.nextInt();
                    BusquedaLimitada.dls(estadoInicial, estadoFinal, limite);
                    break;
                case 5:
                    BusquedaHeuristica.aStar(estadoInicial, estadoFinal);
                    break;
                case 0:
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }

        sc.close();
    }

    public static List<String> obtenerSucesores(String state) {
        List<String> successors = new ArrayList<String>();

        switch (state.indexOf(" ")) {
            case 0: {
                successors.add(state.replace(state.charAt(0), '*').replace(state.charAt(1), state.charAt(0)).replace('*', state.charAt(1)));
                successors.add(state.replace(state.charAt(0), '*').replace(state.charAt(3), state.charAt(0)).replace('*', state.charAt(3)));
                break;
            }
            case 1: {
                successors.add(state.replace(state.charAt(1), '*').replace(state.charAt(0), state.charAt(1)).replace('*', state.charAt(0)));
                successors.add(state.replace(state.charAt(1), '*').replace(state.charAt(2), state.charAt(1)).replace('*', state.charAt(2)));
                successors.add(state.replace(state.charAt(1), '*').replace(state.charAt(4), state.charAt(1)).replace('*', state.charAt(4)));
                break;
            }
            case 2: {

                successors.add(state.replace(state.charAt(2), '*').replace(state.charAt(1), state.charAt(2)).replace('*', state.charAt(1)));
                successors.add(state.replace(state.charAt(2), '*').replace(state.charAt(5), state.charAt(2)).replace('*', state.charAt(5)));
                break;
            }
            case 3: {
                successors.add(state.replace(state.charAt(3), '*').replace(state.charAt(0), state.charAt(3)).replace('*', state.charAt(0)));
                successors.add(state.replace(state.charAt(3), '*').replace(state.charAt(4), state.charAt(3)).replace('*', state.charAt(4)));
                successors.add(state.replace(state.charAt(3), '*').replace(state.charAt(6), state.charAt(3)).replace('*', state.charAt(6)));
                break;
            }
            case 4: {
                successors.add(state.replace(state.charAt(4), '*').replace(state.charAt(1), state.charAt(4)).replace('*', state.charAt(1)));
                successors.add(state.replace(state.charAt(4), '*').replace(state.charAt(3), state.charAt(4)).replace('*', state.charAt(3)));
                successors.add(state.replace(state.charAt(4), '*').replace(state.charAt(5), state.charAt(4)).replace('*', state.charAt(5)));
                successors.add(state.replace(state.charAt(4), '*').replace(state.charAt(7), state.charAt(4)).replace('*', state.charAt(7)));
                break;
            }
            case 5: {
                successors.add(state.replace(state.charAt(5), '*').replace(state.charAt(2), state.charAt(5)).replace('*', state.charAt(2)));
                successors.add(state.replace(state.charAt(5), '*').replace(state.charAt(4), state.charAt(5)).replace('*', state.charAt(4)));
                successors.add(state.replace(state.charAt(5), '*').replace(state.charAt(8), state.charAt(5)).replace('*', state.charAt(8)));
                break;
            }
            case 6: {
                successors.add(state.replace(state.charAt(6), '*').replace(state.charAt(3), state.charAt(6)).replace('*', state.charAt(3)));
                successors.add(state.replace(state.charAt(6), '*').replace(state.charAt(7), state.charAt(6)).replace('*', state.charAt(7)));
                break;

            }
            case 7: {
                successors.add(state.replace(state.charAt(7), '*').replace(state.charAt(4), state.charAt(7)).replace('*', state.charAt(4)));
                successors.add(state.replace(state.charAt(7), '*').replace(state.charAt(6), state.charAt(7)).replace('*', state.charAt(6)));
                successors.add(state.replace(state.charAt(7), '*').replace(state.charAt(8), state.charAt(7)).replace('*', state.charAt(8)));
                break;
            }
            case 8: {
                successors.add(state.replace(state.charAt(8), '*').replace(state.charAt(5), state.charAt(8)).replace('*', state.charAt(5)));
                successors.add(state.replace(state.charAt(8), '*').replace(state.charAt(7), state.charAt(8)).replace('*', state.charAt(7)));
                break;
            }
        }
        return successors;
    }
    public static void imprimirCamino(Map<String, String> padres, String estadoFinal){
        List<String> camino = new ArrayList<>();
        String actual = estadoFinal;
        while (actual != null) {
            camino.add(actual);
            actual = padres.get(actual);
        }
        Collections.reverse(camino);
        int pasos = 0;
        for (String estado : camino) {
            System.out.println("Paso "+pasos++);
            App.imprimirFormato(estado);
        }
        System.out.println("Numero total de movimientos: "+(pasos-1));
    }
    public static void imprimirFormato(String tableroLinea){
        System.out.println(
                " ___________________ \n"
                +"|     |      |      |\n"
                +"|  "+tableroLinea.charAt(0)+"  |"+"   "+tableroLinea.charAt(1)+"  |"+"   "+tableroLinea.charAt(2)+"  |\n"
                +"|_____|______|______|\n"
                +"|     |      |      |\n"
                +"|  "+tableroLinea.charAt(3)+"  |"+"   "+tableroLinea.charAt(4)+"  |"+"   "+tableroLinea.charAt(5)+"  |\n"
                +"|_____|______|______|\n"
                +"|     |      |      |\n"
                +"|  "+tableroLinea.charAt(6)+"  |"+"   "+tableroLinea.charAt(7)+"  |"+"   "+tableroLinea.charAt(8)+"  |\n"
                +"|_____|______|______|\n");
    }
}
