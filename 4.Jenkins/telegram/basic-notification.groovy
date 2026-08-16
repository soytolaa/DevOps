pipeline {
    agent any

    environment {
        TELEGRAM_TOKEN = credentials('TELEGRAM_TOKEN')
        TELEGRAM_CHAT_ID = credentials('TELEGRAM_CHAT_ID')
    }

    stages {

        stage('Send') {
            steps {
                script{
                    def msg="""
                    Hello Jenkins!!!
                    """
                
                                 sh """
                    curl -s -X POST \
                      "https://api.telegram.org/bot\${TELEGRAM_TOKEN}/sendMessage" \
                      --data-urlencode "chat_id=\${TELEGRAM_CHAT_ID}" \
                      --data-urlencode "parse_mode=Markdown" \
                      --data-urlencode "text=${msg}"
                """
                }
            }
        }
    }
}
