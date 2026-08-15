    @Library('my-shared-library') _

    pipeline{

        agent any

        tools{
            nodejs "nodejs-22"
        }

        environment {
            IMAGE_API = "api-img"
            CONTAINER_API = "api-cont"
            FULL_IMAGE_API= "${DH_USER}/${IMAGE_API}:${TAG}"
            DOMAIN_API = "api.soytola.site"
            PORT_API = "8080"
            SPRING_REPO = "https://github.com/soytolaa/task-management-api.git"

            IMAGE_UI = "ui-img"
            CONTAINER_UI = "ui-cont"
            FULL_IMAGE_UI= "${DH_USER}/${IMAGE_UI}:${TAG}"
            DOMAIN_UI = "alien.soytola.site"
            PORT_UI = "3000"
            NEXTJS_REPO = "https://github.com/soytolaa/reactjs-devops.git"

            PORT_NGINX = "80"

            TAG  = "v1"
            DH_USER = "megamind007"
            EMAIL = "m.megamind007@gmail.com"
            TELEGRAM_TOKEN = credentials('TELEGRAM_TOKEN')
            TELEGRAM_CHAT_ID = credentials('TELEGRAM_CHAT_ID')

        }
        parameters{
            choice(
                name: 'PROJECT_TYPE',
                choices: ['spring', 'nextjs'],
                description: 'Select the project type'
            )

        }
        stages{
            stage("Clone"){
                steps{
                    script{
                        
                        echo "Clone Starting..."

                        if(params.PROJECT_TYPE=='spring'){

                            echo "Cloning ${params.PROJECT_TYPE} project..."

                            git(url: env.SPRING_REPO,branch: 'main')

                        }else{
                            echo "Cloning ${params.PROJECT_TYPE} project..."

                            git(url: env.NEXTJS_REPO,branch: 'main')
                        }
                    }

                }
            }
            stage("Test"){

                steps{

                    script {

                        if (params.PROJECT_TYPE == 'spring') {

                            sh '''
                                echo "Building Spring Boot..."

                                mvn clean package -DskipTests
                            '''

                        } else {

                            sh '''
                                echo "Building NextJS..."

                                node -v
                                npm -v
                                
                            '''
                        }
                    }

                }

            }
            stage('SonarQube') {
                steps {
                    script {
                        echo "SonarQube Scaning..."
                        echo "SonarQube project version ${TAG}"
                        echo "SonarQube Type ${params.PROJECT_TYPE}"
                    
                        scanSonarqube(
                            params.PROJECT_TYPE,
                            env.TAG,
                            params.PROJECT_TYPE
                        )
                    }
                }
            }
            stage('Build'){
                steps{
                    script{
                        echo "Bulid Dockerfile..."
                        if(params.PROJECT_TYPE=='spring'){
                            buildDocker(
                                params.PROJECT_TYPE,
                                env.FULL_IMAGE_API
                            )
                        }else{
                            buildDocker(
                                params.PROJECT_TYPE,
                                env.FULL_IMAGE_UI
                            )
                        }

                    }
                }

            }
            stage('Push'){
                steps{
                    script{

                        echo "Bulid Dockerfile..."

                        if(params.PROJECT_TYPE=='spring'){
                            pushDocker(env.FULL_IMAGE_API)
                        }else{
                            pushDocker(env.FULL_IMAGE_UI)
                        }

                    }
                }

            }
            stage('Deploy'){
                steps{
                    script{

                        if(params.PROJECT_TYPE=='spring'){
                            deployDocker(
                                env.CONTAINER_API,
                                env.FULL_IMAGE_API,
                                env.PORT_API.toInteger(),
                                env.PORT_NGINX.toInteger()
                            )
                            configureNginx(
                                params.PROJECT_TYPE,
                                env.DOMAIN_API,
                                env.PORT_API.toInteger(),
                                env.EMAIL
                            )
                        }else{
                            deployDocker(
                                env.CONTAINER_UI,
                                env.FULL_IMAGE_UI,
                                env.PORT_UI.toInteger(),
                                env.PORT_NGINX.toInteger()
                            )
                            configureNginx(
                                params.PROJECT_TYPE,
                                env.DOMAIN_UI,
                                env.PORT_UI.toInteger(),
                                env.EMAIL
                            )
                        }
                    
                    }
                }

            }
        }
        post {

            success {
                script {
                    sendTelegram(
                        """
                            Deployment completed successfully.

                            Project: ${params.PROJECT_TYPE}
                        """,
                        env.TELEGRAM_TOKEN,
                        env.TELEGRAM_CHAT_ID
                    )
                }
            }

            failure {
                script {
                    sendTelegram(
                        """
                            Pipeline failed.

                            Project: ${params.PROJECT_TYPE}

                            The pipeline stopped before the next stage.
                        """,
                        env.TELEGRAM_TOKEN,
                        env.TELEGRAM_CHAT_ID
                    )
                }
            }
        }
    }