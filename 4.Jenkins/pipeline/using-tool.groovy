pipeline {
    agent any

    tools{
        nodejs 'nodejs-20'
    }

    stages {
        stage('Test Tool') {
        when{
            expression{
                params.RUN_TEST == true
            }
        }
            steps {
                sh """
                   node -v
                   npm -v 
                   echo "RUN TEST IS : ${params.RUN_TEST}"
                """
                // git 'https://github.com/keoKAY/reactjs-devop8-template.git'
            }
        }
        stage("Build"){
            steps {
                sh """
                   node -v
                   npm -v 
                """
                // git 'https://github.com/keoKAY/reactjs-devop8-template.git'
            }
        }
      
    }
    
}
