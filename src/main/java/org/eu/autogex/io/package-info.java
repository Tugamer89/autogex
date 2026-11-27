/**
 * Serialization and deserialization utilities for finite state automata.
 *
 * <p>Provides {@link org.eu.autogex.io.AutomatonSerializer} for exporting {@code DFA}, {@code NFA},
 * and {@code ENFA} instances into a canonical, deterministic JSON format and reloading them back
 * into immutable automata with zero external runtime dependencies.
 */
package org.eu.autogex.io;
