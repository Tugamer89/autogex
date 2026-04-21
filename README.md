<p align="center">
  <img src="https://autogex.eu.org/logo.svg" alt="Autogex Logo" width="500">
</p>

# Autogex

**A powerful Java engine for processing Finite State Automata and Regular Expressions.**

![Java Version](https://img.shields.io/badge/Java-25-orange?logo=java)
![Build Status](https://github.com/Tugamer89/autogex/actions/workflows/build.yml/badge.svg)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Tugamer89_autogex&metric=coverage&token=415f1436d3abd3702152559c347907a6f725e6e4)](https://sonarcloud.io/summary/new_code?id=Tugamer89_autogex)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Tugamer89_autogex&metric=alert_status&token=415f1436d3abd3702152559c347907a6f725e6e4)](https://sonarcloud.io/summary/new_code?id=Tugamer89_autogex)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Tugamer89_autogex&metric=sqale_rating&token=415f1436d3abd3702152559c347907a6f725e6e4)](https://sonarcloud.io/summary/new_code?id=Tugamer89_autogex)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Tugamer89_autogex&metric=security_rating&token=415f1436d3abd3702152559c347907a6f725e6e4)](https://sonarcloud.io/summary/new_code?id=Tugamer89_autogex)
![wakatime](https://wakatime.com/badge/user/423e1479-325a-4958-8d21-2d5f97c11efb/project/a34da62c-470e-4ee6-8156-4b61c35c9bde.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
[![Javadoc](https://img.shields.io/badge/Javadoc-Reference-007ec6?logo=java&logoColor=white)](https://autogex.eu.org)
[![Maven Central](https://img.shields.io/maven-central/v/org.eu.autogex/autogex?color=blue&logo=apachemaven&logoColor=white)](https://central.sonatype.com/artifact/org.eu.autogex/autogex)

**Autogex** is a lightweight, clean, and extensible library built to manipulate, convert, and minimize formal computational models. It strictly implements the theorems of Formal Language Theory, providing an extremely intuitive, thread-safe, and immutability-based "Fluent" API (via the Builder Pattern).

## Current Features

- **Regex Compilation:** Full pipeline converting string regular expressions into optimized Minimal DFAs via Abstract Syntax Trees (AST) and Thompson's Construction.
- **Modeling:** Native support for `DFA` (Deterministic Finite Automata), `NFA` (Non-Deterministic), and `ENFA` (ε-NFA with silent transitions).
- **Conversion:**  Subset construction algorithm (Rabin-Scott) and ε-transition elimination (`Converter`).
- **Optimization:** DFA minimization using the Equivalence Classes algorithm (Moore's partitioning) with preemptive cleanup of unreachable states (`Minimizer`).
- **Visualization:** Export any automaton or compiled regex directly to the Graphviz DOT language or Mermaid.js format for native GitHub rendering.
- **Execution Tracing (New!):** Detailed step-by-step visual debugging of an automaton's state traversal with `automaton.execute(input).getFormattedTrace()`.

## Installation

You can include Autogex in your Java project by adding this dependency to your `pom.xml` file:

```xml
<dependency>
  <groupId>org.eu.autogex</groupId>
  <artifactId>autogex</artifactId>
  <version>1.3.7-SNAPSHOT</version>
</dependency>
```

## Usage Example

For a complete overview of all classes and methods, see the [Official API documentation](https://tugamer89.github.io/autogex).

### 1. The Regex Facade & Visualization

The easiest way to use Autogex is through the `Regex` facade. It automatically parses the string, builds the AST, converts to a DFA, and minimizes it.

```java
import org.eu.autogex.regex.Regex;

// Compiles the regex into a theoretical Minimal DFA under the hood
Regex regex = new Regex("(a|b)*abb");

System.out.println(regex.matches("abaabb")); // Output: true

// Export the internal Minimal DFA to Graphviz DOT format for rendering!
String dotGraph = regex.toDotGraph();
System.out.println(dotGraph);

// Export the internal Minimal DFA to Mermaid format for native GitHub Markdown rendering!
String mermaidGraph = regex.toMermaidGraph();
System.out.println(mermaidGraph);
```

*(You can copy the generated `dotGraph` string and paste it into [WebGraphviz](http://www.webgraphviz.com/) to see your state machine!)*

### 2. Manual Automata Construction

For educational purposes or custom needs, you can still manually build and convert models using the Builder pattern.

```java
import org.eu.autogex.models.ENFA;
import org.eu.autogex.algorithms.Converter;
import org.eu.autogex.algorithms.Minimizer;
import org.eu.autogex.models.DFA;

// Building an ENFA that accepts the language: a*b*
ENFA enfa = new ENFA.Builder()
        .addState("q0", true)
        .addState("q1", true)
        .setInitialState("q0")
        .addTransition("q0", 'a', "q0")
        .addEpsilonTransition("q0", "q1") // Silent transition
        .addTransition("q1", 'b', "q1")
        .build();

// Convert the ENFA into a minimal DFA
DFA minimalDfa = Minimizer.minimize(Converter.enfaToDfa(enfa));

System.out.println(minimalDfa.accepts("aaabbb")); // Output: true
```

## License

This project is licensed under the MIT License. Feel free to use, study, modify, and distribute this code, even in commercial projects. See the [`LICENSE`](https://github.com/Tugamer89/autogex/blob/main/LICENSE) for full details.
