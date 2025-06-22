# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ta4k is a technical analysis library for Kotlin providing tools for creating, evaluating, and executing trading
strategies. The project is transitioning from Java to Kotlin and uses Gradle for build management.

## Build Commands

### Standard Operations

- **Build project**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Run single test**: `./gradlew test --tests ClassName.methodName`
- **Clean**: `./gradlew clean`
- **Build specific module**: `./gradlew :ta4j-core:build`
- **Run tests for specific module**: `./gradlew :ta4j-core:test`

### Development Environment

- **Java Version**: 21+ required
- **Kotlin Version**: 2.1.20
- **Gradle Version**: 8.14.2
- **Module structure**: Multi-module Gradle project with ta4j-core, ta4j-examples, ta4j-csv, and ta4j-jdbc modules
- **Version Catalog**: Dependencies managed via `gradle/libs.versions.toml`

## Architecture Overview

### Core Components

**BarSeries**: Central time-series data abstraction containing OHLCV bars. Supports multiple timeframes and real-time
data ingestion via event-driven architecture.

**Indicators**: Stream-processing components with fluent API for chaining operations. Support both numeric (
`NumericIndicator`) and boolean (`BooleanIndicator`) types. All indicators implement stability tracking and lag
calculation.

**Strategies**: Composed of entry and exit rules with multi-timeframe support. Rules use composable boolean logic with
fluent API (`and`, `or`, `negation`).

**Num System**: Abstract numerical layer supporting both `DecimalNum` (precision) and `DoubleNum` (performance). All
calculations flow through this abstraction for consistency.

**Backtesting**: Event replay system with configurable trade execution models, cost modeling, and comprehensive
performance analysis criteria.

**Live Trading**: Real-time processing with `LiveBarSeries` for stateless operation and minimal memory footprint.

### Key Packages

- `org.ta4j.core.api.*` - Core interfaces and contracts
- `org.ta4j.core.indicators.*` - Technical indicator implementations
- `org.ta4j.core.strategy.*` - Trading strategy and rule system
- `org.ta4j.core.backtest.*` - Backtesting and analysis framework
- `org.ta4j.core.num.*` - Numerical abstraction layer
- `org.ta4j.core.trading.*` - Live trading capabilities

### Design Patterns

- **Event-driven**: Components respond to market events (`CandleReceived`, `TickReceived`)
- **Fluent Interface**: Chainable method calls for indicator composition
- **Strategy Pattern**: Interchangeable components (NumFactory, CostModels)
- **Observer Pattern**: Multiple components can react to bar updates

## Testing

The project uses JUnit 5 with AssertJ for assertions. Test files mirror the source structure:

- Unit tests in `src/test/java/org/ta4j/core/`
- Mock objects in `core.mocks` package
- Excel-based test data in `src/test/resources/`

Common test utilities:

- `TestUtils.kt` - Helper methods for test data creation
- `MockStrategy.java` - Predefined strategies for testing
- `CriterionFactory.java` - Factory for analysis criteria testing

## Kotlin Migration Steps

The project is actively migrating from Java to Kotlin. When working on files:

- Create list of classes for migration
- Choose first class and correspoinding test
- Migrate them to Koltin using Kotlin idioms and best practices
- After conversion run tests
- Fix tests
- Ask for permission to continue with next clas

## Code Conventions

### Build System

- **Multi-module Gradle project** with centralized configuration in root `build.gradle.kts`
- **Version catalog** (`gradle/libs.versions.toml`) for dependency management
- **Kotlin DSL** used throughout for type-safe build scripts
- Common configuration applied via `subprojects` block in root build file
- Each module has minimal `build.gradle.kts` with module-specific dependencies only

### Code Style

- Prefer immutable objects and functional programming patterns
- Use meaningful variable names reflecting financial/trading domain
- Follow Kotlin coding conventions for new code

### Testing

- Write comprehensive unit tests for all new indicators and strategies
- Include edge cases and boundary conditions
- Use descriptive test method names explaining the scenario
- Test both stable and unstable indicator states
