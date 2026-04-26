package org.eu.autogex;

/**
 * Main utility class and metadata provider for the AutoGex library.
 *
 * <p>This class provides global information about the library version and ensures that the root
 * package is recognized as non-empty by the Java Module System.
 */
public final class AutoGex {

    /**
     * The current version of the AutoGex library.
     *
     * <p>This value is dynamically retrieved from the JAR manifest (Implementation-Version). If the
     * library is running un-packaged (e.g., during tests), it falls back to a default snapshot
     * value.
     */
    public static final String VERSION =
            resolveVersion(
                    AutoGex.class.getPackage() != null
                            ? AutoGex.class.getPackage().getImplementationVersion()
                            : null);

    /** Private constructor to prevent instantiation of this utility class. */
    private AutoGex() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    /**
     * Dynamically resolves the library version from the package metadata.
     *
     * @param implementationVersion The version string retrieved from the package manifest, or null.
     * @return The implementation version or a fallback default string.
     */
    static String resolveVersion(String implementationVersion) {
        return implementationVersion != null ? implementationVersion : "1.0.0-SNAPSHOT";
    }
}
