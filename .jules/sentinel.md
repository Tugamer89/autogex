## 2024-10-24 - Injection Vulnerability in Graph Renderers
**Vulnerability:** The Graphviz DOT and Mermaid string exporters concatenated raw unsanitized names of states and labels into string-based graphs (`"` and `\` were not escaped).
**Learning:** This leads to XSS/syntax breaking when graphs are natively rendered on platforms (e.g. GitHub native Mermaid support) or passed to local graphing CLI tools.
**Prevention:** Always escape reserved string characters in any user-controlled input before interpolating it into a specific output data format (e.g., DOT, Mermaid, JSON, XML).
