pipeline {
    agent {
        label 'docker'
    }

    stages {
        stage('Parallel Tasks') {
            parallel {

                stage('Task 1') {
                    steps {
                        sh 'echo "Task 1"'
                    }
                }

                stage('Task 2') {
                    steps {
                        sh 'echo "Task 2"'
                    }
                }
            }
        }
    }
}