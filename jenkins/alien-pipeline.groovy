pipeline {
    agent any

    tools{
        nodejs "nodejs-22"
    }

    environment {
        IMAGE_NAME = "alien-img"
        CONTAINER_NAME = "alien-cont"
        IMAGE_TAG  = "v.1.0.0"
        DH_USER = "megamind007"
        FULL_IMAGE= "${DH_USER}/${IMAGE_NAME}:${IMAGE_TAG}"
        DOMAIN_NAME = "alien.soytola.site"
        EMAIL = "m.megamind007@gmail.com"
    }

    triggers {
        githubPush()
    }

    stages {

        stage('User do') {
            steps {
                sh 'echo "I am $(whoami)"'
            }
        }

        stage('Checkout') {
            steps {
                 git(url: 'https://github.com/soytolaa/reactjs-devops.git',branch: 'main')
            }
        }

        stage('Test') {
            steps {
                sh """
                    node -v
                    npm -v
                    npm ci
                """
            }
        }

        stage('Build') {
            steps {
                sh """
                    docker build -t ${FULL_IMAGE} .
                """
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'DH_CRED', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {

                    sh"""
                       echo "${PASSWORD}" | docker login -u ${USERNAME} --password-stdin

                       docker push ${FULL_IMAGE}
                    """ 
   
                }
            }
        }

        stage('Nginx Revers Proxy') {
            steps {
                sh '''
                    sudo ln -sfn \
                        /home/ts/jenkins/config/alien.conf \
                        /etc/nginx/conf.d/alien.conf

                    sudo nginx -t
                    sudo systemctl reload nginx
                '''
            }
        }

        stage('Run & Deploy') {
            steps {
                sh """

                    echo "Docker Stop..."
                    docker stop ${CONTAINER_NAME}|| true

                    echo "Docker Remove..."
                    docker rm ${CONTAINER_NAME} || true

                    echo "Docker Pull..."
                    docker pull ${FULL_IMAGE}

                    echo "Docker Run..."
                    docker run -dp 3000:80 --name ${CONTAINER_NAME} ${FULL_IMAGE}
   
                """
            }
        }

        stage('Domain name') {
            steps {
                sh '''
                    echo "Checking Certbot..."

                    if ! command -v certbot >/dev/null 2>&1; then
                        echo "Certbot not found. Installing..."

                        sudo apt-get update
                        sudo apt-get install -y certbot python3-certbot-nginx
                    else
                        echo "Certbot already installed."
                    fi

                    CERT_PATH="/etc/letsencrypt/live/${DOMAIN_NAME}/fullchain.pem"

                    if [ -f "$CERT_PATH" ]; then
                        echo "SSL certificate already exists."

                        sudo certbot renew

                        sudo nginx -t
                        sudo systemctl reload nginx
                    else
                        echo "SSL certificate not found. Requesting..."

                        sudo certbot --nginx \
                            -d "${DOMAIN_NAME}" \
                            --non-interactive \
                            --agree-tos \
                            -m "${EMAIL}" \
                            --redirect
                    fi

                    echo "Testing SSL renewal..."

                    sudo certbot renew --dry-run
                '''
            }
        }

    }
}