package interpolation;

/**
 * Variable metadata for compile-time analysis.
 */
record VarInfo(String name, // Variable name from template
    int slot, // Local variable slot number as in java bytecode
    boolean isWide, // true for long/double (occupy 2 slots)
    String typeDescriptor, // JVM type descriptor of the variable itself
    String fieldOwner // For fields: owner class (internal name), null for local variables
) {
}
