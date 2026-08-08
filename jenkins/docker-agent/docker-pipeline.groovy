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
                git 'https://github.com/keoKAY/reactjs-devop8-template.git'
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
