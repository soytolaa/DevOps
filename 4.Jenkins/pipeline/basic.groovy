pipeline {
    agent any

    environment {
        IMAGE_NAME = "alien-img"
        CONTAINER_NAME = "alien-cont"
        IMAGE_TAG  = "v.1.0.${env.BUILD_IN}"
        DH_USER = "megamind007"
        FULL_IMAGE= "${DH_USER}/${IMAGE_NAME}:${IMAGE_TAG}"
        DOMAIN_NAME = "alien.soytola.site"
        EMAIL = "m.megamind007@gmail.com"
    }

    stages {

        stage('Clone Code') {
            steps {
                git 'https://github.com/keoKAY/reactjs-devop8-template.git'
            }
        }

        stage('Build') {
            steps {
                sh """
                    docker build -t ${FULL_IMAGE} -f pro.Dockerfile .
                """
            }
        }

        stage('Push to registry'){
            steps{
                withCredentials([usernamePassword(credentialsId: 'DH_CRED', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {

                        sh"""

                        echo "${PASSWORD}" | docker login -u ${USERNAME} --password-stdin

                        docker push ${FULL_IMAGE}

                        """ 
    
                    }
            }

        }

        stage("Deploy Service"){
            steps{
                sh """

                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true
                    docker run -dp 3000:80 --name ${CONTAINER_NAME} ${FULL_IMAGE}

                """
            }
        }

    
    }
    
}
