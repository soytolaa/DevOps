#!/bin/bash

kubectl create secret generic postgres-secret \
  --from-literal=POSTGRES_PASSWORD='1234'




# Imperative