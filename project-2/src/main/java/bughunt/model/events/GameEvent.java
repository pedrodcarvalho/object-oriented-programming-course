package bughunt.model.events;

import bughunt.model.core.Position;

/** Something that happened: its type, where it happened (may be null) and a value (e.g. points). */
public record GameEvent(EventType type, Position position, int value) {
}
