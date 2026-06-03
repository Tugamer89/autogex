import re

filepath = 'src/test/java/org/eu/autogex/models/ENFATest.java'

with open(filepath, 'r') as f:
    content = f.read()

new_content = content.replace('assertFalse(enfa.accepts(null), "ENFA should safely reject null inputs");', 'IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> enfa.accepts(null));\n        assertTrue(exception.getMessage().contains("cannot be null"), "ENFA should safely reject null inputs");')
if 'import static org.junit.jupiter.api.Assertions.assertThrows;' not in new_content:
    new_content = new_content.replace('import static org.junit.jupiter.api.Assertions.assertTrue;', 'import static org.junit.jupiter.api.Assertions.assertThrows;\nimport static org.junit.jupiter.api.Assertions.assertTrue;')

with open(filepath, 'w') as f:
    f.write(new_content)
