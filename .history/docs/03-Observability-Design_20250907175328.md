# Observability & Monitoring Design

## Overview

This document outlines the comprehensive observability strategy for the Swap Life Cycle Management Service, with particular emphasis on thread-level monitoring and performance metrics.

## Monitoring Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Observability Stack                          │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │ Prometheus  │  │   Grafana   │  │    Jaeger   │  │    ELK      │ │
│  │ (Metrics)   │  │ (Dashboards)│  │ (Tracing)   │  │ (Logging)   │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                    Application Metrics                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │   Thread    │  │ Performance │  │  Business   │  │   Health    │ │
│  │  Metrics    │  │  Metrics    │  │  Metrics    │  │   Checks    │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Thread-Level Monitoring

### Thread Pool Metrics

**Core Thread Pools**:
```java
@Component
public class ThreadPoolMetrics {
    private final MeterRegistry meterRegistry;
    
    @PostConstruct
    public void initializeMetrics() {
        // Trade Processing Thread Pool
        Gauge.builder("threadpool.trade.active")
            .description("Number of active threads in trade processing pool")
            .register(meterRegistry, tradeProcessor, ThreadPoolExecutor::getActiveCount);
            
        Gauge.builder("threadpool.trade.core")
            .description("Number of core threads in trade processing pool")
            .register(meterRegistry, tradeProcessor, ThreadPoolExecutor::getCorePoolSize);
            
        Gauge.builder("threadpool.trade.max")
            .description("Maximum number of threads in trade processing pool")
            .register(meterRegistry, tradeProcessor, ThreadPoolExecutor::getMaximumPoolSize);
            
        Gauge.builder("threadpool.trade.queue")
            .description("Number of tasks in trade processing queue")
            .register(meterRegistry, tradeProcessor, ThreadPoolExecutor::getQueue);
            
        // Position Processing Thread Pool
        Gauge.builder("threadpool.position.active")
            .description("Number of active threads in position processing pool")
            .register(meterRegistry, positionProcessor, ThreadPoolExecutor::getActiveCount);
            
        Gauge.builder("threadpool.position.core")
            .description("Number of core threads in position processing pool")
            .register(meterRegistry, positionProcessor, ThreadPoolExecutor::getCorePoolSize);
            
        Gauge.builder("threadpool.position.max")
            .description("Maximum number of threads in position processing pool")
            .register(meterRegistry, positionProcessor, ThreadPoolExecutor::getMaximumPoolSize);
            
        Gauge.builder("threadpool.position.queue")
            .description("Number of tasks in position processing queue")
            .register(meterRegistry, positionProcessor, ThreadPoolExecutor::getQueue);
    }
}
```

**Thread Configuration**:
```yaml
thread-pools:
  trade-processor:
    core-size: 20
    max-size: 50
    queue-capacity: 1000
    keep-alive: 60s
    thread-name-prefix: "trade-processor-"
    
  position-processor:
    core-size: 100
    max-size: 200
    queue-capacity: 5000
    keep-alive: 60s
    thread-name-prefix: "position-processor-"
    
  cashflow-processor:
    core-size: 50
    max-size: 100
    queue-capacity: 2000
    keep-alive: 60s
    thread-name-prefix: "cashflow-processor-"
```

### JVM Thread Metrics

**JVM Thread Monitoring**:
```java
@Component
public class JvmThreadMetrics {
    private final MeterRegistry meterRegistry;
    private final ThreadMXBean threadMXBean;
    
    @Scheduled(fixedRate = 5000)
    public void collectJvmThreadMetrics() {
        // Total thread count
        Gauge.builder("jvm.threads.total")
            .description("Total number of JVM threads")
            .register(meterRegistry, threadMXBean, ThreadMXBean::getThreadCount);
            
        // Peak thread count
        Gauge.builder("jvm.threads.peak")
            .description("Peak number of JVM threads")
            .register(meterRegistry, threadMXBean, ThreadMXBean::getPeakThreadCount);
            
        // Daemon thread count
        Gauge.builder("jvm.threads.daemon")
            .description("Number of daemon threads")
            .register(meterRegistry, threadMXBean, ThreadMXBean::getDaemonThreadCount);
            
        // Deadlocked threads
        Gauge.builder("jvm.threads.deadlocked")
            .description("Number of deadlocked threads")
            .register(meterRegistry, threadMXBean, bean -> {
                long[] deadlockedThreads = bean.findDeadlockedThreads();
                return deadlockedThreads != null ? deadlockedThreads.length : 0;
            });
    }
}
```

## Performance Metrics

### Processing Metrics

**Trade Processing Metrics**:
```java
@Component
public class TradeProcessingMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter tradeProcessedCounter;
    private final Timer tradeProcessingTimer;
    private final Gauge tradeQueueSize;
    
    public TradeProcessingMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.tradeProcessedCounter = Counter.builder("trades.processed")
            .description("Total number of trades processed")
            .register(meterRegistry);
            
        this.tradeProcessingTimer = Timer.builder("trades.processing.time")
            .description("Time taken to process trades")
            .register(meterRegistry);
            
        this.tradeQueueSize = Gauge.builder("trades.queue.size")
            .description("Number of trades in processing queue")
            .register(meterRegistry, this, TradeProcessingMetrics::getQueueSize);
    }
    
    public void recordTradeProcessed(String tradeId, Duration processingTime) {
        tradeProcessedCounter.increment(Tags.of("trade_id", tradeId));
        tradeProcessingTimer.record(processingTime);
    }
}
```

**Position Processing Metrics**:
```java
@Component
public class PositionProcessingMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter positionProcessedCounter;
    private final Timer positionProcessingTimer;
    private final Histogram positionCountHistogram;
    
    public PositionProcessingMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.positionProcessedCounter = Counter.builder("positions.processed")
            .description("Total number of positions processed")
            .register(meterRegistry);
            
        this.positionProcessingTimer = Timer.builder("positions.processing.time")
            .description("Time taken to process positions")
            .register(meterRegistry);
            
        this.positionCountHistogram = Histogram.builder("positions.per.trade")
            .description("Distribution of positions per trade")
            .register(meterRegistry);
    }
    
    public void recordPositionProcessed(String tradeId, int positionCount, Duration processingTime) {
        positionProcessedCounter.increment(Tags.of("trade_id", tradeId));
        positionProcessingTimer.record(processingTime);
        positionCountHistogram.record(positionCount);
    }
}
```

**Cashflow Generation Metrics**:
```java
@Component
public class CashflowMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter cashflowGeneratedCounter;
    private final Timer cashflowGenerationTimer;
    private final Gauge cashflowQueueSize;
    
    public CashflowMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.cashflowGeneratedCounter = Counter.builder("cashflows.generated")
            .description("Total number of cashflows generated")
            .register(meterRegistry);
            
        this.cashflowGenerationTimer = Timer.builder("cashflows.generation.time")
            .description("Time taken to generate cashflows")
            .register(meterRegistry);
            
        this.cashflowQueueSize = Gauge.builder("cashflows.queue.size")
            .description("Number of cashflows in generation queue")
            .register(meterRegistry, this, CashflowMetrics::getQueueSize);
    }
    
    public void recordCashflowGenerated(String tradeId, int cashflowCount, Duration generationTime) {
        cashflowGeneratedCounter.increment(Tags.of("trade_id", tradeId), cashflowCount);
        cashflowGenerationTimer.record(generationTime);
    }
}
```

## Business Metrics

### Trade Lifecycle Metrics

**Trade Status Metrics**:
```java
@Component
public class TradeLifecycleMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter newTradeCounter;
    private final Counter amendmentCounter;
    private final Counter unwindCounter;
    private final Counter terminationCounter;
    
    public TradeLifecycleMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.newTradeCounter = Counter.builder("trades.lifecycle.new")
            .description("Number of new trades created")
            .register(meterRegistry);
            
        this.amendmentCounter = Counter.builder("trades.lifecycle.amendment")
            .description("Number of trade amendments")
            .register(meterRegistry);
            
        this.unwindCounter = Counter.builder("trades.lifecycle.unwind")
            .description("Number of trade unwinds")
            .register(meterRegistry);
            
        this.terminationCounter = Counter.builder("trades.lifecycle.termination")
            .description("Number of trade terminations")
            .register(meterRegistry);
    }
}
```

### Settlement Metrics

**Settlement Instruction Metrics**:
```java
@Component
public class SettlementMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter settlementInstructionCounter;
    private final Timer settlementProcessingTimer;
    private final Counter settlementErrorCounter;
    
    public SettlementMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.settlementInstructionCounter = Counter.builder("settlement.instructions.generated")
            .description("Number of settlement instructions generated")
            .register(meterRegistry);
            
        this.settlementProcessingTimer = Timer.builder("settlement.processing.time")
            .description("Time taken to process settlement instructions")
            .register(meterRegistry);
            
        this.settlementErrorCounter = Counter.builder("settlement.errors")
            .description("Number of settlement processing errors")
            .register(meterRegistry);
    }
}
```

## Health Checks

### Service Health Indicators

**Database Health Check**:
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                return Health.up()
                    .withDetail("database", "MS SQL Server")
                    .withDetail("connection", "Active")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "MS SQL Server")
                    .withDetail("connection", "Invalid")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "MS SQL Server")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

**IBM MQ Health Check**:
```java
@Component
public class MQHealthIndicator implements HealthIndicator {
    private final MQQueueManager queueManager;
    
    @Override
    public Health health() {
        try {
            // Test queue connection
            MQQueue queue = queueManager.accessQueue("SYSTEM.DEFAULT.LOCAL.QUEUE", 
                MQC.MQOO_INPUT_AS_Q_DEF);
            queue.close();
            
            return Health.up()
                .withDetail("mq", "IBM MQ")
                .withDetail("connection", "Active")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("mq", "IBM MQ")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

**Thread Pool Health Check**:
```java
@Component
public class ThreadPoolHealthIndicator implements HealthIndicator {
    private final ThreadPoolExecutor tradeProcessor;
    private final ThreadPoolExecutor positionProcessor;
    
    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        // Trade processor health
        details.put("trade.processor.active", tradeProcessor.getActiveCount());
        details.put("trade.processor.core", tradeProcessor.getCorePoolSize());
        details.put("trade.processor.max", tradeProcessor.getMaximumPoolSize());
        details.put("trade.processor.queue", tradeProcessor.getQueue().size());
        
        // Position processor health
        details.put("position.processor.active", positionProcessor.getActiveCount());
        details.put("position.processor.core", positionProcessor.getCorePoolSize());
        details.put("position.processor.max", positionProcessor.getMaximumPoolSize());
        details.put("position.processor.queue", positionProcessor.getQueue().size());
        
        // Check for thread pool exhaustion
        boolean healthy = tradeProcessor.getActiveCount() < tradeProcessor.getMaximumPoolSize() &&
                         positionProcessor.getActiveCount() < positionProcessor.getMaximumPoolSize();
        
        return healthy ? Health.up().withDetails(details).build() : 
                        Health.down().withDetails(details).build();
    }
}
```

## Grafana Dashboard Configuration

### Thread Monitoring Dashboard

**Thread Pool Overview**:
```json
{
  "dashboard": {
    "title": "Thread Pool Monitoring",
    "panels": [
      {
        "title": "Trade Processor Thread Pool",
        "type": "graph",
        "targets": [
          {
            "expr": "threadpool_trade_active",
            "legendFormat": "Active Threads"
          },
          {
            "expr": "threadpool_trade_core",
            "legendFormat": "Core Threads"
          },
          {
            "expr": "threadpool_trade_max",
            "legendFormat": "Max Threads"
          },
          {
            "expr": "threadpool_trade_queue",
            "legendFormat": "Queue Size"
          }
        ]
      },
      {
        "title": "Position Processor Thread Pool",
        "type": "graph",
        "targets": [
          {
            "expr": "threadpool_position_active",
            "legendFormat": "Active Threads"
          },
          {
            "expr": "threadpool_position_core",
            "legendFormat": "Core Threads"
          },
          {
            "expr": "threadpool_position_max",
            "legendFormat": "Max Threads"
          },
          {
            "expr": "threadpool_position_queue",
            "legendFormat": "Queue Size"
          }
        ]
      }
    ]
  }
}
```

### Performance Dashboard

**Processing Performance**:
```json
{
  "dashboard": {
    "title": "Processing Performance",
    "panels": [
      {
        "title": "Trade Processing Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(trades_processed_total[5m])",
            "legendFormat": "Trades/Second"
          }
        ]
      },
      {
        "title": "Position Processing Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(positions_processed_total[5m])",
            "legendFormat": "Positions/Second"
          }
        ]
      },
      {
        "title": "Cashflow Generation Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(cashflows_generated_total[5m])",
            "legendFormat": "Cashflows/Second"
          }
        ]
      }
    ]
  }
}
```

## Alerting Rules

### Thread Pool Alerts

**Thread Pool Exhaustion Alert**:
```yaml
groups:
  - name: thread-pool-alerts
    rules:
      - alert: ThreadPoolExhaustion
        expr: threadpool_trade_active >= threadpool_trade_max * 0.9
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Trade processor thread pool near exhaustion"
          description: "Trade processor has {{ $value }} active threads out of {{ $value }} max threads"
          
      - alert: PositionThreadPoolExhaustion
        expr: threadpool_position_active >= threadpool_position_max * 0.9
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Position processor thread pool near exhaustion"
          description: "Position processor has {{ $value }} active threads out of {{ $value }} max threads"
```

### Performance Alerts

**Processing Rate Alerts**:
```yaml
groups:
  - name: performance-alerts
    rules:
      - alert: LowTradeProcessingRate
        expr: rate(trades_processed_total[5m]) < 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Low trade processing rate"
          description: "Trade processing rate is {{ $value }} trades/second"
          
      - alert: HighProcessingLatency
        expr: histogram_quantile(0.95, rate(trades_processing_time_seconds_bucket[5m])) > 5
        for: 3m
        labels:
          severity: critical
        annotations:
          summary: "High processing latency"
          description: "95th percentile processing time is {{ $value }} seconds"
```

## Logging Strategy

### Structured Logging

**Application Logs**:
```java
@Component
public class StructuredLogger {
    private final Logger logger = LoggerFactory.getLogger(StructuredLogger.class);
    
    public void logTradeProcessing(String tradeId, String status, Duration processingTime) {
        logger.info("Trade processing completed", 
            StructuredArguments.keyValue("trade_id", tradeId),
            StructuredArguments.keyValue("status", status),
            StructuredArguments.keyValue("processing_time_ms", processingTime.toMillis()),
            StructuredArguments.keyValue("thread_name", Thread.currentThread().getName())
        );
    }
    
    public void logThreadPoolStatus(ThreadPoolExecutor executor, String poolName) {
        logger.info("Thread pool status",
            StructuredArguments.keyValue("pool_name", poolName),
            StructuredArguments.keyValue("active_threads", executor.getActiveCount()),
            StructuredArguments.keyValue("core_threads", executor.getCorePoolSize()),
            StructuredArguments.keyValue("max_threads", executor.getMaximumPoolSize()),
            StructuredArguments.keyValue("queue_size", executor.getQueue().size())
        );
    }
}
```

### Log Configuration

**Logback Configuration**:
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <message/>
                <mdc/>
                <arguments/>
            </providers>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/swap-lifecycle-service.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/swap-lifecycle-service.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <message/>
                <mdc/>
                <arguments/>
            </providers>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```
