
public class App {

    public static void main(String[] args) throws Exception {
        String[] example = { "alejandro", "carlos", "cesar", "kevin" };

        var arbol = new Arbol<String>();

        for (String name : example) {
            arbol.insertar(name);
        }

        // var name = "paco";

        // System.out.println("Existe " + name + " " + arbol.existe(name));

        arbol.imprimir();
    }
}
