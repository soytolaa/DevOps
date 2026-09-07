# Kubernetes Deployment Strategies

Kubernetes provides several deployment strategies for releasing new versions of applications while controlling downtime, availability, and rollout risk.

## Table of Contents

* [1. Recreate](#1-recreate)
* [2. RollingUpdate](#2-rollingupdate)
* [3. Blue-Green Deployment](#3-blue-green-deployment)
* [4. Canary Deployment](#4-canary-deployment)
* [5. A/B Testing](#5-ab-testing)
* [6. Comparison](#6-comparison)
* [7. Recommended Strategy](#7-recommended-strategy)
* [8. Useful kubectl Commands](#8-useful-kubectl-commands)

---

# 1. Recreate

The `Recreate` strategy stops all existing Pods before creating new Pods.

```yaml
spec:
  strategy:
    type: Recreate
```

### Deployment flow

```text
v1 Pods
   ↓
Delete all v1 Pods
   ↓
No Pods running
   ↓
Create v2 Pods
```

Example:

```text
Before:

[v1] [v1] [v1]

       ↓

[Nothing]

       ↓

[v2] [v2] [v2]
```

### Advantages

* Simple to configure
* Only one application version runs at a time
* Useful when different versions cannot run simultaneously

### Disadvantages

* Causes downtime
* Not suitable for highly available applications
* Users cannot access the application while Pods are being recreated

---

# 2. RollingUpdate

`RollingUpdate` gradually replaces old Pods with new Pods.

It is the **default Deployment strategy in Kubernetes**.

```yaml
spec:
  replicas: 3

  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
```

## Deployment flow

With 3 replicas:

```text
Step 1

[v1] [v1] [v1]


Step 2

[v1] [v1] [v1] [v2]


Step 3

[v1] [v1] [v2] [v2]


Step 4

[v1] [v2] [v2] [v2]


Step 5

[v2] [v2] [v2]
```

## maxUnavailable

Defines how many Pods can be unavailable during the update.

```yaml
maxUnavailable: 1
```

With 3 replicas:

```text
3 replicas
   ↓
1 Pod can be unavailable
   ↓
At least 2 Pods remain available
```

## maxSurge

Defines how many additional Pods can temporarily exist during the update.

```yaml
maxSurge: 1
```

With:

```yaml
replicas: 3
```

Kubernetes can temporarily run:

```text
3 desired Pods + 1 extra Pod
= 4 Pods
```

### Advantages

* Usually zero downtime
* Built directly into Kubernetes
* Simple rollback
* Low resource overhead
* Good default for most applications

### Disadvantages

* Old and new versions temporarily run together
* Some applications may not support multiple versions simultaneously
* Rollout can be slower than Blue-Green

---

# 3. Blue-Green Deployment

Blue-Green deployment runs two application versions simultaneously.

```text
                 Service
                    |
             ┌──────┴──────┐
             ↓             ↓
          Blue           Green
           v1              v2
```

## Blue = Current Version

```text
Service
   |
   ↓
[v1] [v1] [v1]
```

## Deploy Green

```text
Service
   |
   ├──→ [v1] [v1] [v1]    Blue
   |
   └──→ [v2] [v2] [v2]    Green
```

The new version can be tested before receiving production traffic.

After validation:

```text
Service
   |
   ↓
[v2] [v2] [v2]
```

Traffic has now switched from Blue to Green.

## Advantages

* Very fast rollback
* Easy to test the new version
* Minimal downtime
* Clear separation between versions

## Disadvantages

* Requires more resources
* Two complete environments may need to run
* More complicated than RollingUpdate

> Blue-Green is not configured using `strategy.type` in a normal Kubernetes Deployment. It is usually implemented using multiple Deployments and Services, or tools such as Argo Rollouts.

---

# 4. Canary Deployment

Canary deployment releases the new version to only a small percentage of users first.

Example:

```text
                  Users
                    |
              Load Balancer
                    |
             ┌──────┴──────┐
             ↓             ↓
           v1              v2
          90%              10%
```

If the new version works correctly:

```text
v1 → 70%
v2 → 30%
```

Then:

```text
v1 → 30%
v2 → 70%
```

Finally:

```text
v2 → 100%
```

## Advantages

* Reduces deployment risk
* Problems can be detected early
* Easy to monitor the new version
* Can gradually increase traffic

## Disadvantages

* More complicated traffic management
* Requires additional routing configuration
* Requires monitoring and observability
* Usually needs an Ingress Controller, service mesh, or rollout controller

Common tools:

* Argo Rollouts
* Istio
* Traefik
* NGINX
* Other service mesh / traffic management solutions

---

# 5. A/B Testing

A/B testing sends different users to different application versions based on specific rules.

Example:

```text
                    Users
                      |
                Traffic Router
                 /           \
                /             \
           Version A       Version B
             50%             50%
```

Unlike a simple Canary deployment, traffic can be split based on characteristics such as:

* User group
* HTTP headers
* Cookies
* Geography
* Device type
* Specific user IDs

Example:

```text
Normal users → v1
Beta users   → v2
```

A/B testing is commonly implemented using:

* Ingress Controllers
* Service Mesh
* API Gateways
* Argo Rollouts

---

# 6. Comparison

| Strategy      | Downtime   | Resource Usage | Rollback  | Complexity |
| ------------- | ---------- | -------------- | --------- | ---------- |
| Recreate      | Yes        | Low            | Slow      | Easy       |
| RollingUpdate | Usually No | Low            | Easy      | Easy       |
| Blue-Green    | Usually No | High           | Very Fast | Medium     |
| Canary        | No         | Medium         | Very Fast | High       |
| A/B Testing   | No         | Medium         | Fast      | High       |

---

# 7. Recommended Strategy

For a typical Spring Boot application running in Kubernetes:

```text
                    Kubernetes Deployment
                             |
                             ↓
                      RollingUpdate
                             |
             ┌───────────────┴───────────────┐
             ↓                               ↓
        maxUnavailable                    maxSurge
             ↓                               ↓
              1                              1
```

Recommended configuration:

```yaml
apiVersion: apps/v1
kind: Deployment

metadata:
  name: spring-dpl

spec:
  replicas: 3

  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1

  selector:
    matchLabels:
      app: spring-app

  template:
    metadata:
      labels:
        app: spring-app

    spec:
      containers:
        - name: spring-container
          image: spring-app:v1
          ports:
            - containerPort: 8080
```

This provides a good balance between:

* Availability
* Simplicity
* Resource usage
* Deployment speed
* Rollback capability

---

# 8. Useful kubectl Commands

## Check Deployment

```bash
kubectl get deployment
```

```bash
kubectl get deployment spring-dpl
```

## Check Pods

```bash
kubectl get pods
```

```bash
kubectl get pods -l app=spring-app
```

## Update Image

```bash
kubectl set image deployment/spring-dpl \
  spring-container=spring-app:v2
```

## Watch Rollout

```bash
kubectl rollout status deployment/spring-dpl
```

## Check Rollout History

```bash
kubectl rollout history deployment/spring-dpl
```

## Roll Back

```bash
kubectl rollout undo deployment/spring-dpl
```

## Check Deployment Details

```bash
kubectl describe deployment spring-dpl
```

## Check ReplicaSets

```bash
kubectl get replicasets
```

---

# Deployment Strategy Flow

```text
                 New Application Version
                           |
                           ↓
                 ┌───────────────────┐
                 │ Deployment Strategy│
                 └─────────┬─────────┘
                           |
          ┌────────────────┼────────────────┐
          ↓                ↓                ↓
      Recreate       RollingUpdate      Blue-Green
          |                |                |
      Stop old         Replace          Run both
       Pods            gradually        versions
          |                |                |
          └────────────────┼────────────────┘
                           ↓
                      Canary / A-B
                           |
                           ↓
                    Gradual Traffic
                      Migration
```

## Learning Path

For Kubernetes beginners, a good order is:

```text
1. Recreate
      ↓
2. RollingUpdate
      ↓
3. Blue-Green
      ↓
4. Canary
      ↓
5. A/B Testing
      ↓
6. Argo Rollouts
```

For production Kubernetes workloads, **RollingUpdate is usually the starting point**, while **Blue-Green and Canary** become useful when you need safer or more controlled releases.
