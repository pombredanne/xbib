package iitb.Model;

public interface EdgeIterator {
    void start();

    boolean hasNext();

    Edge next();

    boolean nextIsOuter();
}
