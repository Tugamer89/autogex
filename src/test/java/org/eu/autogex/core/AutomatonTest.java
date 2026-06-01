package org.eu.autogex.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eu.autogex.trace.ExecutionTrace;
import org.junit.jupiter.api.Test;

class AutomatonTest {

    @Test
    void testDefaultAcceptsReturnsTrueWhenTraceIsAccepted() {
        ExecutionTrace acceptedTraceMock = mock(ExecutionTrace.class);
        when(acceptedTraceMock.isAccepted()).thenReturn(true);

        Automaton automatonMock = mock(Automaton.class);

        when(automatonMock.execute(anyString())).thenReturn(acceptedTraceMock);
        when(automatonMock.accepts(anyString())).thenCallRealMethod();

        assertTrue(
                automatonMock.accepts("test-input"),
                "The default accepts() method should return true when the trace is accepted");
    }

    @Test
    void testDefaultAcceptsReturnsFalseWhenTraceIsRejected() {
        ExecutionTrace rejectedTraceMock = mock(ExecutionTrace.class);
        when(rejectedTraceMock.isAccepted()).thenReturn(false);

        Automaton automatonMock = mock(Automaton.class);

        when(automatonMock.execute(anyString())).thenReturn(rejectedTraceMock);
        when(automatonMock.accepts(anyString())).thenCallRealMethod();

        assertFalse(
                automatonMock.accepts("test-input"),
                "The default accepts() method should return false when the trace is rejected");
    }
}
