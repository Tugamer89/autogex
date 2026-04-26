package org.eu.autogex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class AutoGexTest {

    @Test
    void testVersionIsResolved() {
        // Assert that the version is successfully loaded (either from Manifest or fallback)
        assertNotNull(AutoGex.VERSION, "Version should not be null");
        assertTrue(AutoGex.VERSION.length() > 0, "Version string should not be empty");
    }

    @Test
    void testResolveVersionBranches() {
        // Test the fallback branch (simulating unpackaged/test environment)
        assertEquals(
                "1.0.0-SNAPSHOT",
                AutoGex.resolveVersion(null),
                "Should fallback to snapshot if version is null");

        // Test the primary branch (simulating packaged JAR with manifest)
        assertEquals(
                "2.1.0",
                AutoGex.resolveVersion("2.1.0"),
                "Should use the provided implementation version");
    }

    @Test
    void testUtilityClassCannotBeInstantiated() throws NoSuchMethodException {
        // We use reflection to test the private constructor of the utility class
        Constructor<AutoGex> constructor = AutoGex.class.getDeclaredConstructor();
        // Allow access to the private constructor
        constructor.setAccessible(true);

        // Assert that invoking the private constructor throws an InvocationTargetException
        InvocationTargetException exception =
                assertThrows(
                        InvocationTargetException.class,
                        constructor::newInstance,
                        "Instantiating the utility class should throw an exception");

        // Assert that the cause of the InvocationTargetException is our
        // UnsupportedOperationException
        assertTrue(
                exception.getCause() instanceof UnsupportedOperationException,
                "The cause should be an UnsupportedOperationException");

        // Optionally, check the message
        assertTrue(
                exception.getCause().getMessage().contains("utility class"),
                "Exception message should mention utility class");
    }
}
