# GCP VM Automation with Ansible

## 📌 Overview
This project automates the lifecycle of Google Cloud Platform (GCP) virtual machines using **Ansible**.  
It provisions a cluster consisting of:
- **2 Masters** (`e2-standard-2`, 50GB disk, zone: asia-southeast1-c)
- **2 Workers** (`e2-medium`, 40GB disk, zone: asia-southeast2-a)

The automation also generates a static **Ansible inventory** file using Jinja2 templates, grouping the created VMs into `[masters]` and `[workers]`.  
A destroy playbook is included to clean up all resources.

---
