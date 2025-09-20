# Deployment Architecture

## Overview

This document outlines the deployment architecture for the Swap Life Cycle Management Service, including Docker containerization, Kubernetes orchestration, and environment-specific configurations.

## Container Architecture

### Service Containerization

**Dockerfile Template**:
```dockerfile
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy application JAR
COPY target/*.jar app.jar

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Multi-Stage Build

**Optimized Dockerfile**:
```dockerfile
# Build stage
FROM maven:3.9.4-openjdk-21-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM tuning
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/dumps"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## Kubernetes Deployment

### Namespace Configuration

**Namespace Definition**:
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: swap-lifecycle
  labels:
    name: swap-lifecycle
    environment: production
```

### Service Deployments

**Blotter Ingest Service**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: blotter-ingest-service
  namespace: swap-lifecycle
  labels:
    app: blotter-ingest-service
    version: v1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: blotter-ingest-service
  template:
    metadata:
      labels:
        app: blotter-ingest-service
        version: v1.0.0
    spec:
      containers:
      - name: blotter-ingest-service
        image: swap-lifecycle/blotter-ingest-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: IBM_MQ_HOST
          valueFrom:
            secretKeyRef:
              name: mq-secrets
              key: host
        - name: IBM_MQ_PORT
          valueFrom:
            secretKeyRef:
              name: mq-secrets
              key: port
        - name: IBM_MQ_USERNAME
          valueFrom:
            secretKeyRef:
              name: mq-secrets
              key: username
        - name: IBM_MQ_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mq-secrets
              key: password
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 120
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
      volumes:
      - name: config-volume
        configMap:
          name: blotter-ingest-config
      - name: logs-volume
        persistentVolumeClaim:
          claimName: logs-pvc
```

**Position Management Service**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: position-management-service
  namespace: swap-lifecycle
  labels:
    app: position-management-service
    version: v1.0.0
spec:
  replicas: 5
  selector:
    matchLabels:
      app: position-management-service
  template:
    metadata:
      labels:
        app: position-management-service
        version: v1.0.0
    spec:
      containers:
      - name: position-management-service
        image: swap-lifecycle/position-management-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secrets
              key: url
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: database-secrets
              key: username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: database-secrets
              key: password
        resources:
          requests:
            memory: "4Gi"
            cpu: "2000m"
          limits:
            memory: "8Gi"
            cpu: "4000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 120
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
        - name: dumps-volume
          mountPath: /app/dumps
      volumes:
      - name: config-volume
        configMap:
          name: position-management-config
      - name: logs-volume
        persistentVolumeClaim:
          claimName: logs-pvc
      - name: dumps-volume
        persistentVolumeClaim:
          claimName: dumps-pvc
```

**Cashflow Management Service**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cashflow-management-service
  namespace: swap-lifecycle
  labels:
    app: cashflow-management-service
    version: v1.0.0
spec:
  replicas: 8
  selector:
    matchLabels:
      app: cashflow-management-service
  template:
    metadata:
      labels:
        app: cashflow-management-service
        version: v1.0.0
    spec:
      containers:
      - name: cashflow-management-service
        image: swap-lifecycle/cashflow-management-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: CASHFLOW_DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: cashflow-database-secrets
              key: url
        - name: CASHFLOW_DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: cashflow-database-secrets
              key: username
        - name: CASHFLOW_DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: cashflow-database-secrets
              key: password
        resources:
          requests:
            memory: "8Gi"
            cpu: "4000m"
          limits:
            memory: "16Gi"
            cpu: "8000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 120
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
        - name: dumps-volume
          mountPath: /app/dumps
      volumes:
      - name: config-volume
        configMap:
          name: cashflow-management-config
      - name: logs-volume
        persistentVolumeClaim:
          claimName: logs-pvc
      - name: dumps-volume
        persistentVolumeClaim:
          claimName: dumps-pvc
```

### Service Configuration

**Service Definitions**:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: blotter-ingest-service
  namespace: swap-lifecycle
  labels:
    app: blotter-ingest-service
spec:
  selector:
    app: blotter-ingest-service
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  type: ClusterIP

---
apiVersion: v1
kind: Service
metadata:
  name: position-management-service
  namespace: swap-lifecycle
  labels:
    app: position-management-service
spec:
  selector:
    app: position-management-service
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  type: ClusterIP

---
apiVersion: v1
kind: Service
metadata:
  name: cashflow-management-service
  namespace: swap-lifecycle
  labels:
    app: cashflow-management-service
spec:
  selector:
    app: cashflow-management-service
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

### ConfigMaps

**Application Configuration**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: blotter-ingest-config
  namespace: swap-lifecycle
data:
  application.yml: |
    spring:
      application:
        name: blotter-ingest-service
      profiles:
        active: production
    
    server:
      port: 8080
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: always
    
    logging:
      level:
        com.synthetics.core: INFO
        org.springframework: WARN
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
        file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
      file:
        name: /app/logs/blotter-ingest-service.log
    
    ibm-mq:
      connection:
        host: ${IBM_MQ_HOST}
        port: ${IBM_MQ_PORT}
        channel: SYSTEM.DEF.SVRCONN
        queue-manager: QMGR1
        username: ${IBM_MQ_USERNAME}
        password: ${IBM_MQ_PASSWORD}
      queues:
        lifecycle-events: LIFECYCLE.EVENTS
        exceptions: EXCEPTIONS.QUEUE
    
    thread-pools:
      blotter-processor:
        core-size: 10
        max-size: 20
        queue-capacity: 1000
        keep-alive: 60s
        thread-name-prefix: "blotter-processor-"
```

### Secrets Management

**Database Secrets**:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: database-secrets
  namespace: swap-lifecycle
type: Opaque
data:
  url: <base64-encoded-database-url>
  username: <base64-encoded-username>
  password: <base64-encoded-password>

---
apiVersion: v1
kind: Secret
metadata:
  name: cashflow-database-secrets
  namespace: swap-lifecycle
type: Opaque
data:
  url: <base64-encoded-cashflow-database-url>
  username: <base64-encoded-username>
  password: <base64-encoded-password>

---
apiVersion: v1
kind: Secret
metadata:
  name: mq-secrets
  namespace: swap-lifecycle
type: Opaque
data:
  host: <base64-encoded-mq-host>
  port: <base64-encoded-mq-port>
  username: <base64-encoded-mq-username>
  password: <base64-encoded-mq-password>
```

## Horizontal Pod Autoscaler

**HPA Configuration**:
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: cashflow-management-hpa
  namespace: swap-lifecycle
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: cashflow-management-service
  minReplicas: 5
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

## Persistent Volumes

**PVC Definitions**:
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: logs-pvc
  namespace: swap-lifecycle
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 100Gi
  storageClassName: fast-ssd

---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: dumps-pvc
  namespace: swap-lifecycle
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 50Gi
  storageClassName: fast-ssd
```

## Network Policies

**Network Security**:
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: swap-lifecycle-network-policy
  namespace: swap-lifecycle
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: swap-lifecycle
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: swap-lifecycle
    ports:
    - protocol: TCP
      port: 8080
  - to: []
    ports:
    - protocol: TCP
      port: 1433  # SQL Server
    - protocol: TCP
      port: 1414  # IBM MQ
    - protocol: TCP
      port: 53    # DNS
    - protocol: UDP
      port: 53    # DNS
```

## Monitoring and Observability

### Prometheus Configuration

**ServiceMonitor**:
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: swap-lifecycle-services
  namespace: swap-lifecycle
  labels:
    app: swap-lifecycle-services
spec:
  selector:
    matchLabels:
      app: swap-lifecycle-services
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
```

### Grafana Dashboard

**Dashboard ConfigMap**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboard-swap-lifecycle
  namespace: swap-lifecycle
  labels:
    grafana_dashboard: "1"
data:
  dashboard.json: |
    {
      "dashboard": {
        "title": "Swap Life Cycle Management Service",
        "panels": [
          {
            "title": "Service Health",
            "type": "stat",
            "targets": [
              {
                "expr": "up{job=\"swap-lifecycle-services\"}",
                "legendFormat": "Service Status"
              }
            ]
          },
          {
            "title": "Thread Pool Metrics",
            "type": "graph",
            "targets": [
              {
                "expr": "threadpool_trade_active",
                "legendFormat": "Trade Processor Active"
              },
              {
                "expr": "threadpool_position_active",
                "legendFormat": "Position Processor Active"
              },
              {
                "expr": "threadpool_cashflow_active",
                "legendFormat": "Cashflow Processor Active"
              }
            ]
          }
        ]
      }
    }
```

## Environment-Specific Configurations

### Development Environment

**Docker Compose**:
```yaml
version: '3.8'
services:
  blotter-ingest-service:
    build: ./blotter-ingest-service
    ports:
      - "8081:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - IBM_MQ_HOST=mq-server
      - IBM_MQ_PORT=1414
      - IBM_MQ_USERNAME=admin
      - IBM_MQ_PASSWORD=password
    depends_on:
      - mq-server
      - sql-server
    
  position-management-service:
    build: ./position-management-service
    ports:
      - "8082:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - DATABASE_URL=jdbc:sqlserver://sql-server:1433;databaseName=SwapLifecycleDB
      - DATABASE_USERNAME=sa
      - DATABASE_PASSWORD=YourStrong@Passw0rd
    depends_on:
      - sql-server
    
  cashflow-management-service:
    build: ./cashflow-management-service
    ports:
      - "8083:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - CASHFLOW_DATABASE_URL=jdbc:sqlserver://sql-server:1433;databaseName=CashflowDB
      - CASHFLOW_DATABASE_USERNAME=sa
      - CASHFLOW_DATABASE_PASSWORD=YourStrong@Passw0rd
    depends_on:
      - sql-server
    
  mq-server:
    image: ibmcom/mq:latest
    ports:
      - "1414:1414"
    environment:
      - LICENSE=accept
      - MQ_QMGR_NAME=QMGR1
      - MQ_ADMIN_PASSWORD=password
    volumes:
      - mq-data:/mnt/mqm
    
  sql-server:
    image: mcr.microsoft.com/mssql/server:2022-latest
    ports:
      - "1433:1433"
    environment:
      - ACCEPT_EULA=Y
      - SA_PASSWORD=YourStrong@Passw0rd
    volumes:
      - sql-data:/var/opt/mssql
    
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana

volumes:
  mq-data:
  sql-data:
  prometheus-data:
  grafana-data:
```

### Production Environment

**Production Values**:
```yaml
# values-production.yaml
replicaCount: 8
image:
  repository: swap-lifecycle/cashflow-management-service
  tag: "v1.0.0"
  pullPolicy: IfNotPresent

resources:
  requests:
    memory: "8Gi"
    cpu: "4000m"
  limits:
    memory: "16Gi"
    cpu: "8000m"

autoscaling:
  enabled: true
  minReplicas: 5
  maxReplicas: 20
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

nodeSelector: {}
tolerations: []
affinity: {}

ingress:
  enabled: true
  className: "nginx"
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
  hosts:
    - host: swap-lifecycle.company.com
      paths:
        - path: /
          pathType: Prefix
  tls: []
```

## Deployment Strategy

### Rolling Updates

**Deployment Strategy**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cashflow-management-service
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 2
  replicas: 8
  # ... rest of deployment spec
```

### Blue-Green Deployment

**Blue-Green Strategy**:
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: cashflow-management-service
spec:
  replicas: 8
  strategy:
    blueGreen:
      activeService: cashflow-management-service-active
      previewService: cashflow-management-service-preview
      autoPromotionEnabled: false
      scaleDownDelaySeconds: 30
      prePromotionAnalysis:
        templates:
        - templateName: success-rate
        args:
        - name: service-name
          value: cashflow-management-service-preview
      postPromotionAnalysis:
        templates:
        - templateName: success-rate
        args:
        - name: service-name
          value: cashflow-management-service-active
  selector:
    matchLabels:
      app: cashflow-management-service
  template:
    metadata:
      labels:
        app: cashflow-management-service
    spec:
      containers:
      - name: cashflow-management-service
        image: swap-lifecycle/cashflow-management-service:latest
        # ... rest of container spec
```
