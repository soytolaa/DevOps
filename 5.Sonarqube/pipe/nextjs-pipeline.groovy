pipeline {
    agent any

    tools {
        nodejs "nodejs-22"
    }

    environment {
        TELEGRAM_TOKEN = credentials('TELEGRAM_TOKEN')
        TELEGRAM_CHAT_ID = credentials('TELEGRAM_CHAT_ID')
    }

    stages {

        stage("Checkout") {
            steps {
                git(
                    branch: 'main',
                    url: 'https://github.com/soytolaa/reactjs-devops.git'
                )
            }
        }

        stage("Scan with SonarQube") {
            environment {
                scannerHome = tool "sonar-scanner"
            }

            steps {
                withSonarQubeEnv(
                    credentialsId: 'SONAR_TOKEN',
                    installationName: 'sonar-scanner'
                ) {

                    script {
                        def projectName = "NextJS"
                        def projectVersion = "1.0.0"
                        def projectKey = "nextjs"

                        sh """
                            ${scannerHome}/bin/sonar-scanner \
                            -Dsonar.projectName=${projectName} \
                            -Dsonar.projectKey=${projectKey} \
                            -Dsonar.projectVersion=${projectVersion}
                        """
                    }
                }
            }
        }

        stage("Wait for Quality Gate") {
            steps {
                script {

                    def qg = waitForQualityGate()

                    echo "Quality Gate Status: ${qg.status}"

                    if (qg.status != "OK") {
                        error "Quality Gate failed: ${qg.status}"
                    }

                    echo "Quality Gate passed!"
                }
            }
        }
    }

    post {
        success {
            sendTelegram(
                "SUCCESS",
                "Jenkins build and SonarQube Quality Gate passed.",
                env.TELEGRAM_TOKEN,
                env.TELEGRAM_CHAT_ID
            )
        }

        failure {
            sendTelegram(
                "FAILED",
                "Jenkins build or SonarQube Quality Gate failed.",
                env.TELEGRAM_TOKEN,
                env.TELEGRAM_CHAT_ID
            )
        }

        aborted {
            sendTelegram(
                "ABORTED",
                "Jenkins build was aborted.",
                env.TELEGRAM_TOKEN,
                env.TELEGRAM_CHAT_ID
            )
        }
    }
}


// ==============================
// Telegram Function
// ==============================

def sendTelegram(String status, String message,String token,String chatId) {

    def text = """
🚀 Jenkins Notification

Status: ${status}
Job: ${env.JOB_NAME}
Build: #${env.BUILD_NUMBER}

${message}

URL: ${env.BUILD_URL}
""".stripIndent()

    sh """
        curl -s -X POST \
        "https://api.telegram.org/bot${token}/sendMessage" \
        -d chat_id="${chatId}" \
        --data-urlencode text='${text}'
    """
}