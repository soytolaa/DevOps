pipeline {
    agent any

    environment {
        CHAT_ID="1764348762"
        TOKEN="8838380796:AAEdZ6PSIeipaoqdfkiuslH1f6vJ1BL4igk"
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
                      "https://api.telegram.org/bot\${TOKEN}/sendMessage" \
                      --data-urlencode "chat_id=\${CHAT_ID}" \
                      --data-urlencode "parse_mode=Markdown" \
                      --data-urlencode "text=${msg}"
                """
                }
            }
        }
    }
}
