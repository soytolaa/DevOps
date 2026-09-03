```bash

# Set up ansible
sudo apt update
sudo apt install software-properties-common
sudo add-apt-repository --yes --update ppa:ansible/ansible
sudo apt install ansible


# Ansible Vault

ansible-vault create group_vars/secrets.yaml # Create Vault file
ansible-vault encrypt group_vars/secrets.yaml # Encrypt existing file
ansible-vault view group_vars/secrets.yaml # View decrypted content
ansible-vault edit group_vars/secrets.yaml # Edit encrypted file
ansible-playbook -i inventory.ini playbooks/secrets.yaml --ask-vault-pass # ask vault password to input serect key to encrypt
ansible-playbook -i inventory.ini playbooks/demo_vault.yaml --vault-password-file group_vars/vault_pass.txt # get key to encrypt no need to input manaully


```
