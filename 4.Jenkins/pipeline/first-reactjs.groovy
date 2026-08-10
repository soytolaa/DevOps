pipeline {
    agent any

    stages {
        stage('Clone Code') {
            steps {
                git 'https://github.com/keoKAY/reactjs-devop8-template.git'
            }
        }
        stage('Build') {
            steps {
                sh """
                    docker build -t jenkins-react:latest -f prod.Dockerfile .
                """
            }
        }
        stage('Deploy') {
            steps {
                sh """
                    docker stop reactjs-app || true
                    docker rm reactjs-app || true
                    docker run -dp 3000:80 --name reactjs-app jenkins-react:latest

                    
                """
            }
        } 
    }
    
}
