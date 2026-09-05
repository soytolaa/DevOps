## Working with secrets 

- create a secret from kubectl 
```bash 

kubectl create secret generic \
    postgres-secret \
    --from-literal=POSTGRES_PASSWORD='pass12345'

# password will automatically encode to base64 
kubectl get secret   #  to view your secret 

kubectl get secret postgres-secret -n default -o json | jq '.data | map_values(@base64d)'

```

## Working with private images (Private Registry)
```bash
docker pull ghcr.io/zoeistad/nginx-demo:v1.0.0

kubectl create secret docker-registry ghcr-secret \
--docker-server=ghcr.io \
--docker-username=zoeistad \
--docker-password="your-github-token"
```
> GITHUB TOKEN: allow access for `read-registry`