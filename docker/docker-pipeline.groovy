pipeline {
    agent{
        docker{
            image "node:26.7.0"
            args "-u root"
        }
    }

    stages {
        stage('Clone Code') {
            steps {
                git 'https://github.com/soytolaa/reactjs-devops.git'
            }
        }
        stage('Running npm') {
            steps {
                sh """
                    node -v
                    npm -v
                    ls -lrt
                """
            }
        }
    
    }
    
}
