import java.lang.Comparable;

public class Arbol<T extends Comparable<T>> {
    private Nodo<T> root;

    public Arbol() {
    }

    public boolean estaVacio() {
        return this.root == null;
    }

    public boolean existe(T seeked) {
        return buscar(seeked) != null;
    }

    public void insertar(T data) {
        if (estaVacio()){
            this.root = new Nodo<T>(data);
            return;
        }

        insertar(this.root, new Nodo<T>(data));
    }

    private void insertar(Nodo<T> root, Nodo<T> child) {
        if (child.getData().compareTo(root.getData()) <= 0) {
            if (root.getIzq() == null)
                root.setIzq(child);
            else
                insertar(root.getIzq(), child);
        } else {
            if (root.getDer() == null)
                root.setDer(child);
            else
                insertar(root.getDer(), child);
        }
    }

    public T buscar(T seeked) {
        if (estaVacio())
            return null;

        if (this.root.getData().compareTo(seeked) == 0)
            return this.root.getData();

        return buscar(this.root, seeked);
    }

    private T buscar(Nodo<T> root, T seeked) {
        if (root == null)
            return null;

        if (root.getData().compareTo(seeked) == 0) {
            return seeked;
        }
        if (root.getData().compareTo(seeked) < 0) {
            return buscar(root.getIzq(), seeked);
        } else {
            return buscar(root.getDer(), seeked);
        }
    }

    public void imprimir() {
        if (estaVacio())
            return;

        imprimir(this.root);
    }

    private void imprimir(Nodo<T> root) {
        if (root == null)
            return;

        imprimir(root.getIzq());
        System.out.println(root.getData().toString());
        imprimir(root.getDer());
    }
}
