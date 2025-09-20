# Swap Life Cycle Management Service - Architecture Overview

## Executive Summary

The Swap Life Cycle Management Service is a high-performance, event-driven platform designed to handle the complete lifecycle of swap trades from ingestion to settlement. Built with Java 21 and Spring Boot 3.2.2, it processes up to 250K trades with 5K positions per trade and 65K lots per position within a 1-2 hour processing window.

## Core Architecture Principles

### 1. Event-Driven Architecture with Event Sourcing
- **Single Source of Truth**: Event store maintains complete audit trail
- **Deterministic Rebuilds**: Aggregate state can be reconstructed from events
- **Idempotency**: All operations are idempotent with unique event IDs
- **Audit Compliance**: Complete lineage tracking with signatures

### 2. Scalability Design
- **Target Scale**: 250K trades, 5K positions/underliers per trade, 65K lots per position
- **Processing Window**: 1-2 hours (35-70 trades/second)
- **Parallel Processing**: Contract-level and position-level parallelism
- **Horizontal Scaling**: Stateless services with shared event store

### 3. Technology Stack
- **Runtime**: Java 21 with Spring Boot 3.2.2
- **Messaging**: IBM MQ with abstraction layer
- **Database**: MS SQL Server (Trade DB + Cashflow DB)
- **Containerization**: Docker + Kubernetes
- **Observability**: Prometheus/Grafana with detailed thread metrics

## Service Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Swap Life Cycle Management Platform           │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │   Blotter   │  │  Position   │  │  Cashflow   │  │ Reference   │ │
│  │   Ingest    │  │ Management  │  │ Management  │  │    Data      │ │
│  │  Service    │  │  Service    │  │  Service    │  │   Proxy      │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    IBM MQ Event Streaming                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │lifecycle.   │  │position.    │  │cashflows.   │  │settlement.  │ │
│  │events       │  │updates      │  │generated    │  │instructions │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Data Layer                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │   Trade     │  │  Event      │  │  Cashflow   │  │ Reference   │ │
│  │  Database   │  │   Store     │  │  Database   │  │   Data      │ │
│  │ (MS SQL)    │  │ (MS SQL)    │  │ (MS SQL)    │  │   Cache     │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Key Design Decisions

### 1. IBM MQ Integration
- **Abstraction Layer**: Business logic decoupled from messaging implementation
- **Queue Management**: Dedicated queues for each event type
- **Message Persistence**: Guaranteed delivery with acknowledgments
- **Dead Letter Queues**: Exception handling and retry mechanisms

### 2. Proxy Reference Data Implementation
- **Interface-Based Design**: Pluggable reference data providers
- **Mock Implementation**: For development and testing
- **Real Implementation**: Seamless replacement when available
- **Caching Strategy**: Redis-based caching for performance

### 3. Observability & Monitoring
- **Thread Metrics**: Per-container thread usage monitoring
- **Performance Metrics**: Processing rates, latency, throughput
- **Business Metrics**: Trade counts, cashflow volumes, error rates
- **Health Checks**: Service health and dependency status

## Data Flow Architecture

### Event Flow
1. **Blotter Ingest**: Raw blotters → Normalized lifecycle events
2. **Position Management**: Lifecycle events → Trade aggregates → Position updates
3. **Cashflow Management**: Position updates → Cashflow generation → Settlement instructions
4. **Settlement Publisher**: Settlement instructions → Settlement system

### Parallel Processing Flow
```
Trade Level (250K trades)
├── Position Level (5K positions per trade)
│   ├── Lot Level (65K lots per position)
│   └── Cashflow Calculation (per position)
└── Settlement Instruction Generation
```

## Performance Characteristics

### Scalability Targets
- **Throughput**: 35-70 trades/second
- **Latency**: <500ms P95 for REST APIs, <10ms P99 for internal calls
- **Availability**: 99.9% uptime
- **Data Volume**: 250K trades × 5K positions × 65K lots = ~81B data points

### Resource Requirements
- **CPU**: High-performance multi-core processors
- **Memory**: 32GB+ per service instance
- **Storage**: SSD-based storage for databases
- **Network**: High-bandwidth, low-latency network

## Security & Compliance

### Authentication & Authorization
- **OAuth2/OIDC**: API authentication
- **RBAC**: Role-based access control
- **Audit Logging**: Complete operation audit trail

### Data Protection
- **Encryption**: At rest and in transit
- **PII Protection**: Data masking and anonymization
- **Retention Policies**: Configurable data retention

## Deployment Architecture

### Container Strategy
- **Docker Containers**: Each service in separate container
- **Kubernetes**: Orchestration and scaling
- **Service Mesh**: Istio for service communication
- **Config Management**: Kubernetes ConfigMaps and Secrets

### Environment Strategy
- **Development**: Local Docker Compose
- **Testing**: Kubernetes cluster with test data
- **Production**: Multi-zone Kubernetes deployment
