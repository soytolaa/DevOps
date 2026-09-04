# Kubernetes Notes

## 1. kubectl Configuration

Configure `kubectl` so you can run Kubernetes commands without `sudo`.

```bash
mkdir -p ~/.kube

sudo cp /etc/kubernetes/admin.conf ~/.kube/config

sudo chown $(id -u):$(id -g) ~/.kube/config

chmod 600 ~/.kube/config
```

Test:

```bash
kubectl get nodes
```

Check cluster:

```bash
kubectl cluster-info
kubectl version
```

Check current context:

```bash
kubectl config current-context
kubectl config view
```

---

# 2. Kubernetes Nodes

A **Node** is a machine that runs Kubernetes workloads.

```bash
kubectl get nodes
kubectl get nodes -o wide
```

Detailed information:

```bash
kubectl describe node <node-name>
```

Example:

```bash
kubectl describe node ts-worker01
```

---

# 3. Namespace

A **Namespace** provides logical separation between Kubernetes resources.

List namespaces:

```bash
kubectl get namespace
```

Short form:

```bash
kubectl get ns
```

Create namespace:

```bash
kubectl create namespace dev
```

Get resources in a namespace:

```bash
kubectl get pods -n dev
kubectl get deployments -n dev
kubectl get services -n dev
```

Get resources from all namespaces:

```bash
kubectl get pods -A
```

---

# 4. Pod

> **Pod is the smallest deployable unit in Kubernetes.**

A Pod contains one or more containers.

Most commonly:

```text
Pod
└── Container
```

A Pod can also contain multiple containers:

```text
Pod
├── Application Container
└── Sidecar Container
```

Get Pods:

```bash
kubectl get pods
```

Short form:

```bash
kubectl get po
```

Get Pods with more information:

```bash
kubectl get pods -o wide
```

Get Pods from all namespaces:

```bash
kubectl get pods -A
```

Create a Pod from YAML:

```bash
kubectl apply -f pod.yaml
```

Delete a Pod:

```bash
kubectl delete pod <pod-name>
```

Describe a Pod:

```bash
kubectl describe pod <pod-name>
```

---

# 5. Pod Logs

View logs:

```bash
kubectl logs <pod-name>
```

Follow logs:

```bash
kubectl logs -f <pod-name>
```

Example:

```bash
kubectl logs dpl-app-867ccd5945-4sc92
```

If the Pod contains multiple containers:

```bash
kubectl logs <pod-name> -c <container-name>
```

Previous container logs:

```bash
kubectl logs <pod-name> --previous
```

---

# 6. Execute Commands Inside a Pod

Open a shell:

```bash
kubectl exec -it <pod-name> -- /bin/sh
```

Example:

```bash
kubectl exec -it dpl-app-867ccd5945-4sc92 -- /bin/sh
```

If Bash exists:

```bash
kubectl exec -it <pod-name> -- /bin/bash
```

For a specific container:

```bash
kubectl exec -it <pod-name> -c <container-name> -- /bin/sh
```

Run a command without opening a shell:

```bash
kubectl exec <pod-name> -- ls
```

Example:

```bash
kubectl exec <pod-name> -- nginx -t
```

Exit the container:

```bash
exit
```

---

# 7. Deployment

A **Deployment** manages Pods and ReplicaSets.

Instead of manually creating multiple Pods:

```text
Pod
Pod
Pod
```

we define:

```yaml
replicas: 3
```

Kubernetes manages them for us.

Architecture:

```text
Deployment
    │
    ▼
ReplicaSet
    │
    ├── Pod
    ├── Pod
    └── Pod
```

Get Deployments:

```bash
kubectl get deployments
```

Short form:

```bash
kubectl get deploy
```

Create/update Deployment:

```bash
kubectl apply -f deployment.yaml
```

Describe:

```bash
kubectl describe deployment <deployment-name>
```

Delete:

```bash
kubectl delete deployment <deployment-name>
```

---

# 8. Replicas

A **replica** represents the desired number of Pod instances.

Example:

```yaml
spec:
  replicas: 3
```

Means Kubernetes should maintain:

```text
Deployment
    │
    ▼
ReplicaSet
    │
    ├── Pod 1
    ├── Pod 2
    └── Pod 3
```

Check:

```bash
kubectl get pods
```

Scale manually:

```bash
kubectl scale deployment dpl-app --replicas=5
```

Scale down:

```bash
kubectl scale deployment dpl-app --replicas=2
```

Check Deployment:

```bash
kubectl get deployment
```

---

# 9. ReplicaSet

A **ReplicaSet** ensures that the desired number of Pods are running.

Check ReplicaSets:

```bash
kubectl get replicasets
```

Short form:

```bash
kubectl get rs
```

Example:

```text
Deployment
     │
     ▼
ReplicaSet
     │
 ┌───┼───┐
 ▼   ▼   ▼
Pod Pod Pod
```

Normally, you don't create ReplicaSets directly.

You create a **Deployment**, and the Deployment creates and manages the ReplicaSet.

---

# 10. Labels

Labels are key-value pairs used to identify and organize Kubernetes resources.

Example:

```yaml
metadata:
  labels:
    app: dpl-app
```

Get Pods with a label:

```bash
kubectl get pods -l app=dpl-app
```

Show labels:

```bash
kubectl get pods --show-labels
```

---

# 11. Selectors

Selectors allow Kubernetes objects to find other objects using labels.

Example:

```yaml
selector:
  matchLabels:
    app: dpl-app
```

The Deployment uses this selector to identify the Pods it manages.

The Pod must have the matching label:

```yaml
labels:
  app: dpl-app
```

Think:

```text
Deployment
    │
    │ selector: app=dpl-app
    ▼
Pods
    │
    ├── app=dpl-app
    ├── app=dpl-app
    └── app=dpl-app
```

---

# 12. Service

A **Service** provides stable networking for Pods.

Pods are temporary and their IP addresses can change.

Instead of accessing:

```text
Pod IP → 10.244.1.10
```

use:

```text
Service → Pods
```

Architecture:

```text
Client
  │
  ▼
Service
  │
  ├── Pod
  ├── Pod
  └── Pod
```

Get Services:

```bash
kubectl get services
```

Short form:

```bash
kubectl get svc
```

Describe:

```bash
kubectl describe service <service-name>
```

---

# 13. Service Types

Common Service types:

### ClusterIP

Default type.

```yaml
type: ClusterIP
```

Only accessible inside the Kubernetes cluster.

```text
Pod → Service → Pod
```

---

### NodePort

Exposes the Service through a port on each Node.

```yaml
type: NodePort
```

Example:

```text
Node IP:30080
      │
      ▼
 Service
      │
 ├── Pod
 ├── Pod
 └── Pod
```

---

### LoadBalancer

Usually used with cloud providers.

```yaml
type: LoadBalancer
```

Example:

```text
Internet
    │
    ▼
Cloud Load Balancer
    │
    ▼
Service
    │
 ┌──┼──┐
 ▼  ▼  ▼
Pod Pod Pod
```

---

# 14. Port Forward

Useful for testing an application locally.

```bash
kubectl port-forward pod/<pod-name> 8080:80
```

Example:

```bash
kubectl port-forward deployment/dpl-app 8080:80
```

Then:

```bash
curl http://localhost:8080
```

---

# 15. YAML

Kubernetes resources are commonly defined using YAML.

Example Pod:

```yaml
apiVersion: v1
kind: Pod

metadata:
  name: nginx-pod

spec:
  containers:
    - name: nginx
      image: nginx:latest
      ports:
        - containerPort: 80
```

Create:

```bash
kubectl apply -f pod.yaml
```

Delete:

```bash
kubectl delete -f pod.yaml
```

---

# 16. Useful kubectl Commands

Get almost everything:

```bash
kubectl get all
```

Get Pods:

```bash
kubectl get pods
```

Get Deployments:

```bash
kubectl get deployments
```

Get ReplicaSets:

```bash
kubectl get replicasets
```

Get Services:

```bash
kubectl get services
```

Get resources with YAML:

```bash
kubectl get pod <pod-name> -o yaml
```

Get resources with JSON:

```bash
kubectl get pod <pod-name> -o json
```

Watch resources:

```bash
kubectl get pods -w
```

---

# 17. Troubleshooting

Check Pod status:

```bash
kubectl get pods
```

Detailed information:

```bash
kubectl describe pod <pod-name>
```

Check logs:

```bash
kubectl logs <pod-name>
```

Check events:

```bash
kubectl get events
```

Sort events by time:

```bash
kubectl get events --sort-by=.lastTimestamp
```

Check Deployment:

```bash
kubectl describe deployment <deployment-name>
```

Check ReplicaSet:

```bash
kubectl describe rs <replicaset-name>
```

---

# 18. Kubernetes Object Relationship

The most important relationship to understand:

```text
                    Kubernetes Cluster
                           │
                  ┌────────┴────────┐
                  │                 │
                Node              Node
                  │                 │
                Pod               Pod
                  │
             Container
```

For applications:

```text
Deployment
     │
     ▼
ReplicaSet
     │
     ├──────── Pod
     ├──────── Pod
     └──────── Pod
                  │
               Container
```

For networking:

```text
              Service
                 │
          ┌──────┼──────┐
          ▼      ▼      ▼
        Pod    Pod     Pod
```

---

# 19. Basic Workflow

Typical Kubernetes workflow:

```text
1. Create YAML
       ↓
2. kubectl apply
       ↓
3. Deployment
       ↓
4. ReplicaSet
       ↓
5. Pods
       ↓
6. Containers
       ↓
7. Service
       ↓
8. Access application
```

Commands:

```bash
kubectl apply -f deployment.yaml

kubectl get deployment

kubectl get rs

kubectl get pods -o wide

kubectl logs <pod-name>

kubectl exec -it <pod-name> -- /bin/sh

kubectl apply -f service.yaml

kubectl get svc
```

---

# 20. Important Difference

| Object         | Purpose                          |
| -------------- | -------------------------------- |
| **Node**       | Machine running workloads        |
| **Namespace**  | Logical separation               |
| **Pod**        | Smallest deployable unit         |
| **Container**  | Runs the application             |
| **Deployment** | Manages application Pods         |
| **ReplicaSet** | Maintains desired Pod count      |
| **Replica**    | Desired number of Pod copies     |
| **Service**    | Stable network endpoint for Pods |
| **Label**      | Identifies resources             |
| **Selector**   | Finds resources by labels        |

### Mental model

```text
Cluster
│
├── Node
│   ├── Pod
│   │   └── Container
│   │
│   └── Pod
│       └── Container
│
└── Node
    └── Pod
        └── Container


Deployment
    ↓
ReplicaSet
    ↓
Pods
    ↓
Containers

Service
    ↓
Pods
```

This is the foundation you want solid before moving into **ConfigMap → Secret → Volume/PV/PVC → Ingress → StatefulSet → DaemonSet → RBAC → HPA → Helm**.
