import os

files_to_update = [
    'src/main/java/org/eu/autogex/models/DFA.java',
    'src/main/java/org/eu/autogex/models/NFA.java',
    'src/main/java/org/eu/autogex/models/ENFA.java'
]

for filepath in files_to_update:
    with open(filepath, 'r') as f:
        content = f.read()

    new_content = content.replace('if (input == null) {\n            return false;\n        }', 'if (input == null) {\n            throw new IllegalArgumentException("Input string cannot be null.");\n        }')

    with open(filepath, 'w') as f:
        f.write(new_content)

files_to_update = [
    'src/test/java/org/eu/autogex/models/DFATest.java',
    'src/test/java/org/eu/autogex/models/NFATest.java',
    'src/test/java/org/eu/autogex/models/ENFATest.java'
]

import re

for filepath in files_to_update:
    with open(filepath, 'r') as f:
        content = f.read()

    if 'DFATest.java' in filepath:
        new_content = content.replace('assertFalse(dfa.accepts(null), "DFA should safely reject null inputs");', 'IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> dfa.accepts(null));\n        assertTrue(exception.getMessage().contains("cannot be null"), "DFA should safely reject null inputs");')
        if 'import static org.junit.jupiter.api.Assertions.assertThrows;' not in new_content:
            new_content = new_content.replace('import static org.junit.jupiter.api.Assertions.assertTrue;', 'import static org.junit.jupiter.api.Assertions.assertThrows;\nimport static org.junit.jupiter.api.Assertions.assertTrue;')
    elif 'NFATest.java' in filepath:
         new_content = content.replace('assertFalse(nfa.accepts(null), "NFA should safely reject null inputs");', 'IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> nfa.accepts(null));\n        assertTrue(exception.getMessage().contains("cannot be null"), "NFA should safely reject null inputs");')
         if 'import static org.junit.jupiter.api.Assertions.assertThrows;' not in new_content:
            new_content = new_content.replace('import static org.junit.jupiter.api.Assertions.assertTrue;', 'import static org.junit.jupiter.api.Assertions.assertThrows;\nimport static org.junit.jupiter.api.Assertions.assertTrue;')
    elif 'ENFATest.java' in filepath:
         new_content = content.replace('assertFalse(enfa.accepts(null), "ENFA should safely reject null inputs");', 'IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> enfa.accepts(null));\n        assertTrue(exception.getMessage().contains("cannot be null"), "ENFA should safely reject null inputs");')
         if 'import static org.junit.jupiter.api.Assertions.assertThrows;' not in new_content:
            new_content = new_content.replace('import static org.junit.jupiter.api.Assertions.assertTrue;', 'import static org.junit.jupiter.api.Assertions.assertThrows;\nimport static org.junit.jupiter.api.Assertions.assertTrue;')

    with open(filepath, 'w') as f:
        f.write(new_content)
