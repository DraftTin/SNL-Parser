# SNL-Parser

The **SNL-Parser** project implements a parser for the Small Nested Language (SNL). The parser analyzes and interprets code written in SNL, generating a syntax tree and supporting lexical and syntax analysis. It is developed in Kotlin and makes use of modular programming for various compiler phases.

---

## Features

- **Lexical Analysis**: Tokenizes the input source code into meaningful elements (tokens) using the `Lexer`.
- **Syntax Analysis**: Constructs a syntax tree from tokens based on the grammar rules of SNL.
- **Code Analysis Utilities**: Provides helper functions, such as `prettyPrint`, to display the structure of the syntax tree.

---

## Core Components

- **Lexer**: Extracts tokens from the input source code.
- **Syntax Tree**: Represents the hierarchical structure of the code.
- **Program.kt**: The main entry point, which demonstrates the parsing process and includes utilities for debugging.

---

## Usage

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd SNL-Parser
   ```

2. Build the project using Gradle:

   ```bash
   ./gradlew build
   ```

3. Run the parser:

   ```bash
   ./gradlew run
   ```

4. Analyze output or logs in the terminal to understand the syntax tree and lexical tokens.

## File Structure

- src/main/kotlin: Contains the main source code files.
  - Program.kt: Main file to run the parser.
  - CodeAnalysis/: Contains the lexical and syntax analysis modules.
- test.txt: Sample SNL code for testing the parser.

## Example

The parser processes SNL code files and outputs their syntax tree. A sample function, prettyPrint, demonstrates tree visualization.



Example output:

```
└── Root
    ├── Keyword: "if"
    ├── Identifier: "condition"
    └── Block
        ├── Statement: "print"
```

