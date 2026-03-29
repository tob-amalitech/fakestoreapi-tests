pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    environment {
        // Credentials stored in Jenkins → Manage Jenkins → Credentials
        SLACK_WEBHOOK_URL = credentials('SLACK_WEBHOOK_URL')
        MAIL_USERNAME     = credentials('MAIL_USERNAME')
        MAIL_PASSWORD     = credentials('MAIL_PASSWORD')
        MAIL_RECIPIENT    = credentials('MAIL_RECIPIENT')

        // Project info
        PROJECT_NAME      = 'FakeStore API Tests'
        REPORT_DIR        = 'target/surefire-reports'
    }

    options {
        // Keep last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Timeout entire pipeline after 15 minutes
        timeout(time: 15, unit: 'MINUTES')
        // Add timestamps to console output
        timestamps()
        // Don't run concurrent builds on the same branch
        disableConcurrentBuilds()
    }

    triggers {
        // Webhook trigger (GitHub will call this via the webhook)
        // Also poll as a fallback every 5 minutes
        pollSCM('H/5 * * * *')
    }

    stages {

        // ─────────────────────────────────────────
        // Stage 1: Checkout Code from GitHub
        // ─────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo '📥 Pulling latest code from GitHub...'
                checkout scm
                echo "✅ Checked out branch: ${env.GIT_BRANCH}"
                echo "📝 Commit: ${env.GIT_COMMIT}"
            }
        }

        // ─────────────────────────────────────────
        // Stage 2: Build — Download Dependencies
        // ─────────────────────────────────────────
        stage('Build') {
            steps {
                echo '🔨 Installing Maven dependencies...'
                sh 'mvn dependency:resolve -B -q'
                echo '✅ Dependencies resolved successfully'
            }
        }

        // ─────────────────────────────────────────
        // Stage 3: Run Test Suite
        // ─────────────────────────────────────────
        stage('Run Tests') {
            steps {
                echo '🧪 Running FakeStore API test suite...'
                sh 'mvn clean test -B'
            }
            post {
                always {
                    echo '📊 Test execution complete'
                }
            }
        }

        // ─────────────────────────────────────────
        // Stage 4: Publish JUnit XML Report
        // ─────────────────────────────────────────
        stage('Publish JUnit Report') {
            steps {
                echo '📋 Publishing JUnit test results...'
                junit allowEmptyResults: true,
                      testResults: '**/target/surefire-reports/*.xml'
            }
        }

        // ─────────────────────────────────────────
        // Stage 5: Publish HTML Report
        // ─────────────────────────────────────────
        stage('Publish HTML Report') {
            steps {
                echo '🌐 Publishing HTML test report...'
                publishHTML(target: [
                    allowMissing          : false,
                    alwaysLinkToLastBuild : true,
                    keepAll               : true,
                    reportDir             : "${REPORT_DIR}",
                    reportFiles           : 'index.html',
                    reportName            : 'Surefire Test Report',
                    reportTitles          : 'FakeStore API Test Results'
                ])
            }
        }

        // ─────────────────────────────────────────
        // Stage 6: Archive Artifacts
        // ─────────────────────────────────────────
        stage('Archive Reports') {
            steps {
                echo '📦 Archiving test report artifacts...'
                archiveArtifacts artifacts: 'target/surefire-reports/**/*',
                                 allowEmptyArchive: true,
                                 fingerprint: true
            }
        }
    }

    // ─────────────────────────────────────────
    // Post-build: Notifications
    // ─────────────────────────────────────────
    post {

        always {
            echo "🔔 Build finished with status: ${currentBuild.currentResult}"

            // Always send email notification
            emailext(
                subject: "[Jenkins] ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2 style="color: ${currentBuild.currentResult == 'SUCCESS' ? '#27ae60' : '#e74c3c'}">
                            ${currentBuild.currentResult == 'SUCCESS' ? '✅' : '❌'} 
                            Build ${currentBuild.currentResult}
                        </h2>
                        <table style="border-collapse: collapse; width: 100%;">
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><b>Job</b></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">${env.JOB_NAME}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><b>Build #</b></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">${env.BUILD_NUMBER}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><b>Branch</b></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">${env.GIT_BRANCH ?: 'N/A'}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><b>Commit</b></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">${env.GIT_COMMIT ?: 'N/A'}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><b>Duration</b></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">${currentBuild.durationString}</td>
                            </tr>
                        </table>
                        <br/>
                        <a href="${env.BUILD_URL}" 
                           style="background:#3498db;color:white;padding:10px 20px;text-decoration:none;border-radius:4px;">
                            View Build
                        </a>
                        &nbsp;
                        <a href="${env.BUILD_URL}testReport" 
                           style="background:#2ecc71;color:white;padding:10px 20px;text-decoration:none;border-radius:4px;">
                            View Test Report
                        </a>
                    </body>
                    </html>
                """,
                to: "${env.MAIL_RECIPIENT}",
                mimeType: 'text/html',
                attachLog: false
            )
        }

        success {
            script {
                sendSlackNotification('SUCCESS')
            }
            echo '✅ Pipeline completed successfully!'
        }

        failure {
            script {
                sendSlackNotification('FAILURE')
            }
            echo '❌ Pipeline failed!'
        }

        unstable {
            script {
                sendSlackNotification('UNSTABLE')
            }
            echo '⚠️ Pipeline is unstable (some tests failed)!'
        }

        fixed {
            echo '🎉 Build is fixed after previous failure!'
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Helper: Send Slack Notification via Incoming Webhook
// ─────────────────────────────────────────────────────────────
def sendSlackNotification(String status) {
    def emoji   = status == 'SUCCESS' ? '✅' : status == 'FAILURE' ? '❌' : '⚠️'
    def color   = status == 'SUCCESS' ? '#27ae60' : status == 'FAILURE' ? '#e74c3c' : '#f39c12'
    def message = """
    {
        "attachments": [
            {
                "color": "${color}",
                "blocks": [
                    {
                        "type": "header",
                        "text": {
                            "type": "plain_text",
                            "text": "${emoji} Jenkins Build ${status}"
                        }
                    },
                    {
                        "type": "section",
                        "fields": [
                            { "type": "mrkdwn", "text": "*Job:*\\n${env.JOB_NAME}" },
                            { "type": "mrkdwn", "text": "*Build #:*\\n${env.BUILD_NUMBER}" },
                            { "type": "mrkdwn", "text": "*Branch:*\\n${env.GIT_BRANCH ?: 'N/A'}" },
                            { "type": "mrkdwn", "text": "*Duration:*\\n${currentBuild.durationString}" }
                        ]
                    },
                    {
                        "type": "actions",
                        "elements": [
                            {
                                "type": "button",
                                "text": { "type": "plain_text", "text": "View Build" },
                                "url": "${env.BUILD_URL}"
                            },
                            {
                                "type": "button",
                                "text": { "type": "plain_text", "text": "Test Report" },
                                "url": "${env.BUILD_URL}testReport"
                            }
                        ]
                    }
                ]
            }
        ]
    }
    """

    sh """
        curl -s -X POST \\
            -H 'Content-type: application/json' \\
            --data '${message.replaceAll("'", "\\\\'")}' \\
            ${SLACK_WEBHOOK_URL}
    """
}
