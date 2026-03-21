# Autogex

**Un potente motore Java per l'elaborazione di Automi a Stati Finiti ed Espressioni Regolari.**

![Java Version](https://img.shields.io/badge/Java-25-orange?logo=java)
![Build Status](https://github.com/Tugamer89/autogex/actions/workflows/build.yml/badge.svg)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Tugamer89_autogex&metric=coverage&token=415f1436d3abd3702152559c347907a6f725e6e4)](https://sonarcloud.io/summary/new_code?id=Tugamer89_autogex)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Tugamer89_autogex&metric=alert_status&token=415f1436d3abd3702152559c347907a6f725e6e4)](https://sonarcloud.io/summary/new_code?id=Tugamer89_autogex)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Tugamer89_autogex&metric=sqale_rating&token=415f1436d3abd3702152559c347907a6f725e6e4)](https://sonarcloud.io/summary/new_code?id=Tugamer89_autogex)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Tugamer89_autogex&metric=security_rating&token=415f1436d3abd3702152559c347907a6f725e6e4)](https://sonarcloud.io/summary/new_code?id=Tugamer89_autogex)
![wakatime](https://wakatime.com/badge/user/423e1479-325a-4958-8d21-2d5f97c11efb/project/a34da62c-470e-4ee6-8156-4b61c35c9bde.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

**Autogex** è una libreria leggera, pulita ed estensibile nata per manipolare, convertire e minimizzare modelli computazionali formali. Implementa rigorosamente i teoremi della Teoria dei Linguaggi, fornendo un'API "Fluent" (tramite Builder Pattern) estremamente intuitiva, thread-safe e basata sull'immutabilità.

## Funzionalità Attuali

- **Modellazione:** Supporto nativo per `DFA` (Automi Deterministici), `NFA` (Non Deterministici) ed `ENFA` ($\epsilon$-NFA con transizioni silenti).
- **Conversione:** Algoritmo di costruzione per sottoinsiemi (Rabin-Scott) ed eliminazione delle $\epsilon$-transizioni (`Converter`).
- **Ottimizzazione:** Minimizzazione dei DFA tramite l'algoritmo delle Classi di Equivalenza (partizionamento di Moore) con pulizia preventiva degli stati irraggiungibili (`Minimizer`).

## Installazione

Puoi includere Autogex nel tuo progetto Java aggiungendo questa dipendenza al tuo file `pom.xml`:

```xml
<dependency>
    <groupId>it.tugamer89</groupId>
    <artifactId>autogex</artifactId>
    <version>0.2.0</version>
</dependency>
```

**Nota per GitHub Packages:** Per scaricare i pacchetti ospitati direttamente su GitHub, devi avere un *Personal Access Token (PAT)* configurato nel tuo file `~/.m2/settings.xml` locale.

## Esempi di Utilizzo

### 1. Creare un NFA con Epsilon-Transizioni

L'API utilizza il pattern Builder per rendere la dichiarazione degli automi il più leggibile e sicura possibile.

```java
import it.tugamer89.autogex.models.ENFA;

// Costruzione di un ENFA che accetta il linguaggio: a*b*
ENFA enfa = new ENFA.Builder()
        .addState("q0", true)
        .addState("q1", true)
        .setInitialState("q0")
        .addTransition("q0", 'a', "q0")
        .addEpsilonTransition("q0", "q1") // Transizione silente verso q1
        .addTransition("q1", 'b', "q1")
        .build();

System.out.println(enfa.accepts("aaabbb")); // Output: true
System.out.println(enfa.accepts("ba"));     // Output: false
```

### 2. Convertire e Minimizzare

Con poche righe di codice puoi trasformare un automa non deterministico in un automa deterministico perfetto ed ottimizzato, pronto per essere utilizzato ad altissime prestazioni.

```java
import it.tugamer89.autogex.algorithms.Converter;
import it.tugamer89.autogex.algorithms.Minimizer;
import it.tugamer89.autogex.models.DFA;

// 1. Convertiamo l'ENFA precedente in un DFA (Subset Construction)
DFA dfa = Converter.enfaToDfa(enfa);

// 2. Riduciamo il DFA al suo minimo numero teorico di stati
DFA minimalDfa = Minimizer.minimize(dfa);

System.out.println(minimalDfa.accepts("aaabbb")); // Output: true
```

## Roadmap del Progetto

Il progetto è sviluppato per milestone sequenziali:

- [x] **Fase 1:** Modelli Core e Builder (`DFA`, `NFA`, `ENFA`).
- [x] **Fase 2:** Motore di Conversione (Subset Construction) e Minimizzazione.
- [ ] **Fase 3:** Albero Sintattico Astratto (AST) e Parsing di Espressioni Regolari (Regex).
- [ ] **Fase 4:** Compilazione (Algoritmo di Thompson per trasformare Regex $\rightarrow$ ENFA).

## Licenza

Questo progetto è distribuito sotto licenza **MIT**. Sentiti libero di utilizzare, studiare, modificare e distribuire questo codice, anche in progetti commerciali. Vedi il file `LICENSE` per i dettagli completi.
