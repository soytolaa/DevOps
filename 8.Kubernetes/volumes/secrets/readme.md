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