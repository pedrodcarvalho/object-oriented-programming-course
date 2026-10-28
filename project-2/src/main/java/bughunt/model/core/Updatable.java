package bughunt.model.core;

/** Something that changes on every game tick. */
public interface Updatable {
    void update(Match match);
}
