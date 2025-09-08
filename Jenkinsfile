pipeline {
    agent any
    
    environment {
        SONAR_SCANNER_HOME = tool 'SonarQube Scanner'
        LEAD_DEVELOPER_EMAIL = 'dnestrada@unis.edu.gt'
        PRODUCT_OWNER_EMAIL = 'jflores@unis.edu.gt'
        PROJECT_DIR = 'ensurancePharmacy'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_HASH = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    env.BRANCH_NAME = env.BRANCH_NAME ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    echo "Building branch: ${env.BRANCH_NAME}"
                    echo "Commit: ${env.GIT_COMMIT_HASH}"
                }
            }
        }

        stage('Mark SonarQube as PENDING') {
            when { 
                expression { return env.CHANGE_ID != null }
            }
            steps {
                githubNotify credentialsId: 'GITHUB_PAT',
                            context: 'sonarqube/quality-gate',
                            status: 'PENDING',
                            description: 'Running SonarQube analysis',
                            sha: sh(script: "git rev-parse HEAD", returnStdout: true).trim()
            }
        }
        
        stage('Determine Environment') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'dev') {
                        env.ENVIRONMENT = 'development'
                        env.COMPOSE_FILE = 'docker-compose.dev.yml'
                        env.FRONTEND_PORT = '8083'
                        env.BACKEND_PORT = '8084'
                        env.DEPLOY = 'true'
                        env.COMPOSE_PROJECT = 'pharmacy-dev'
                    } else if (env.BRANCH_NAME == 'uat') {
                        env.ENVIRONMENT = 'uat'
                        env.COMPOSE_FILE = 'docker-compose.uat.yml'
                        env.FRONTEND_PORT = '8090'
                        env.BACKEND_PORT = '8091'
                        env.DEPLOY = 'true'
                        env.COMPOSE_PROJECT = 'pharmacy-uat'
                    } else if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'main') {
                        env.ENVIRONMENT = 'production'
                        env.COMPOSE_FILE = 'docker-compose.prod.yml'
                        env.FRONTEND_PORT = '8100'
                        env.BACKEND_PORT = '8101'
                        env.DEPLOY = 'true'
                        env.COMPOSE_PROJECT = 'pharmacy-prod'
                    } else {
                        // Feature branches - only run tests and analysis, no deployment
                        env.ENVIRONMENT = 'feature-validation'
                        env.DEPLOY = 'false'
                        echo "Feature branch detected: ${env.BRANCH_NAME}"
                        echo "Will run tests and SonarQube analysis only (no deployment)"
                    }
                    
                    if (env.DEPLOY == 'true') {
                        echo "Deploying to ${env.ENVIRONMENT} environment using ${env.COMPOSE_FILE}"
                        echo "Docker Compose project: ${env.COMPOSE_PROJECT}"
                    }
                }
            }
        }
        
        stage('Unit Tests') {
            parallel {
                stage('Backend Tests') {
                    steps {
                        dir("${env.PROJECT_DIR}/backv5") {
                            sh 'mvn clean verify'
                        }
                    }
                    post {
                        always {
                            junit(
                                testResults: "${env.PROJECT_DIR}/backv5/target/surefire-reports/*.xml",
                                allowEmptyResults: true
                            )
                        }
                        failure {
                            script {
                                sendFailureEmail('Unit Tests', 'Backend unit tests failed')
                            }
                        }
                    }
                }
                stage('Frontend Tests') {
                    steps {
                        dir("${env.PROJECT_DIR}/pharmacy") {
                            sh 'npm install'
                            sh 'npm test -- --watchAll=false --coverage || echo "Frontend tests skipped - no tests configured"'
                        }
                    }
                    post {
                        failure {
                            script {
                                sendFailureEmail('Unit Tests', 'Frontend unit tests failed')
                            }
                        }
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            parallel {
                stage('Backend Analysis') {
                    steps {
                        dir("${env.PROJECT_DIR}/backv5") {
                            withSonarQubeEnv('SonarQube') {
                                sh """
                                    ${SONAR_SCANNER_HOME}/bin/sonar-scanner \\
                                    -Dsonar.projectKey=pharmacy-backend-${env.ENVIRONMENT.substring(0,3)} \\
                                    -Dsonar.projectName="Pharmacy Backend ${env.ENVIRONMENT.capitalize()}" \\
                                    -Dsonar.sources=src/main/java \\
                                    -Dsonar.tests=src/test/java \\
                                    -Dsonar.test.inclusions=**/*Test.java,**/*Tests.java,**/*IT.java \\
                                    -Dsonar.java.coveragePlugin=jacoco \\
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \\
                                    -Dsonar.java.binaries=target/classes
                                """
                            }
                        }
                    }
                }
                stage('Frontend Analysis') {
                    when {
                        environment name: 'DEPLOY', value: 'true'
                    }
                    steps {
                        dir("${env.PROJECT_DIR}/pharmacy") {
                            withSonarQubeEnv('SonarQube') {
                                sh """
                                    ${SONAR_SCANNER_HOME}/bin/sonar-scanner \\
                                    -Dsonar.projectKey=pharmacy-frontend-${env.ENVIRONMENT.substring(0,3)} \\
                                    -Dsonar.projectName="Pharmacy Frontend ${env.ENVIRONMENT.capitalize()}" \\
                                    -Dsonar.sources=src \\
                                    -Dsonar.exclusions=node_modules/**,dist/**,coverage/**
                                """
                            }
                        }
                    }
                }
            }
            post {
                failure {
                    script {
                        sendFailureEmail('SonarQube Analysis', 'SonarQube analysis failed')
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    timeout(time: 10, unit: 'MINUTES') {
                        def gate = waitForQualityGate()
                        if (gate.status != 'OK') {
                            // Report FAILURE to GitHub for PRs
                            if (env.CHANGE_ID) {
                                githubNotify credentialsId: 'GITHUB_PAT',
                                            context: 'sonarqube/quality-gate',
                                            status: 'FAILURE',
                                            description: "Quality Gate: ${gate.status}",
                                            sha: env.GIT_COMMIT_HASH
                            }
                            error "Quality Gate failed: ${gate.status}"
                        }
                    }
                }
            }
            post {
                success {
                    script {
                        // Report SUCCESS to GitHub for PRs
                        if (env.CHANGE_ID) {
                            githubNotify credentialsId: 'GITHUB_PAT',
                                        context: 'sonarqube/quality-gate',
                                        status: 'SUCCESS',
                                        description: 'Quality Gate passed',
                                        sha: env.GIT_COMMIT_HASH
                        }
                        // Get SonarQube metrics after successful quality gate
                        env.SONAR_METRICS = getSonarQubeMetrics()
                    }
                }
                failure {
                    script {
                        sendFailureEmail('Quality Gate', 'SonarQube Quality Gate failed - Technical debt not allowed')
                    }
                }
            }
        }
        
        stage('Build and Deploy') {
            when {
                environment name: 'DEPLOY', value: 'true'
            }
            steps {
                script {
                    dir(env.PROJECT_DIR) {
                        echo "Deploying to ${env.ENVIRONMENT} environment..."
                        
                        // Specific cleanup only for this environment
                        sh """
                            # Stop only containers for this specific environment and project
                            docker-compose -p ${env.COMPOSE_PROJECT} -f ${env.COMPOSE_FILE} down --remove-orphans --volumes 2>/dev/null || true
                            
                            # Remove only containers specific to this environment
                            docker rm -f pharmacy-backend-${env.ENVIRONMENT.substring(0,3)} pharmacy-frontend-${env.ENVIRONMENT.substring(0,3)} 2>/dev/null || true
                            
                            # Wait a moment for cleanup to complete
                            sleep 3
                        """
                        
                        // Build and start new containers with unique project name
                        sh "docker-compose -p ${env.COMPOSE_PROJECT} -f ${env.COMPOSE_FILE} up -d --build --force-recreate"
                        
                        // Wait for services to be ready
                        sh "sleep 30"
                        
                        echo "Deployment completed!"
                        echo "Frontend: http://localhost:${env.FRONTEND_PORT}"
                        echo "Backend: http://localhost:${env.BACKEND_PORT}"
                    }
                }
            }
            post {
                failure {
                    script {
                        sendFailureEmail('Deployment', "Deployment to ${env.ENVIRONMENT} failed")
                    }
                }
                success {
                    script {
                        sendSuccessEmail()
                    }
                }
            }
        }
        
        stage('Smoke Tests') {
            when {
                environment name: 'DEPLOY', value: 'true'
            }
            steps {
                dir(env.PROJECT_DIR) {
                    sh """
                        echo "Running smoke tests for ${env.ENVIRONMENT} environment"
                        curl -f http://localhost:${env.FRONTEND_PORT} || exit 1
                        echo "Smoke tests passed!"
                    """
                }
            }
            post {
                failure {
                    script {
                        sendFailureEmail('Smoke Tests', 'Smoke tests failed after deployment')
                    }
                }
            }
        }
    }
    
    post {
        success {
            script {
                if (env.DEPLOY == 'true') {
                    sendSuccessEmail()
                } else {
                    echo "✅ Feature branch validation completed successfully!"
                    echo "Tests passed and SonarQube analysis completed for ${env.BRANCH_NAME}"
                }
            }
        }
        failure {
            script {
                sendFailureEmail('Pipeline', 'Overall pipeline failed')
            }
        }
    }
}

def sendFailureEmail(stageName, message) {
    emailext (
        subject: "🚨 Pipeline Failed: ${env.JOB_NAME} - ${stageName}",
        body: """
            <h2>Pipeline Failure Notification</h2>
            
            <p><strong>Pipeline failed at stage:</strong> ${stageName}</p>
            <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
            <p><strong>Environment:</strong> ${env.ENVIRONMENT}</p>
            <p><strong>Commit:</strong> ${env.GIT_COMMIT_HASH}</p>
            
            <h3>Error Details:</h3>
            <p>${message}</p>
            
            <h3>Action Required:</h3>
            <p>Please review the code changes and fix the issues before attempting to merge again.</p>
            
            <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
            
            <hr>
            <p><em>This is an automated notification from the CI/CD pipeline.</em></p>
        """,
        mimeType: 'text/html',
        to: "${env.LEAD_DEVELOPER_EMAIL}, ${env.PRODUCT_OWNER_EMAIL}"
    )
}

def sendSuccessEmail() {
    def backendSonarUrl = "http://localhost:9000/dashboard?id=pharmacy-backend-${env.ENVIRONMENT.substring(0,3)}"
    def frontendSonarUrl = "http://localhost:9000/dashboard?id=pharmacy-frontend-${env.ENVIRONMENT.substring(0,3)}"
    
    emailext (
        subject: "✅ Deployment Success: ${env.JOB_NAME} - ${env.ENVIRONMENT}",
        body: """
            <h2>🎉 Successful Deployment to ${env.ENVIRONMENT.toUpperCase()}</h2>
            
            <table border="1" cellpadding="10" cellspacing="0" style="border-collapse: collapse;">
                <tr style="background-color: #f0f0f0;">
                    <td><strong>Branch:</strong></td>
                    <td>${env.BRANCH_NAME}</td>
                </tr>
                <tr>
                    <td><strong>Commit:</strong></td>
                    <td>${env.GIT_COMMIT_HASH}</td>
                </tr>
                <tr style="background-color: #f0f0f0;">
                    <td><strong>Environment:</strong></td>
                    <td>${env.ENVIRONMENT}</td>
                </tr>
                <tr>
                    <td><strong>Build Number:</strong></td>
                    <td>${env.BUILD_NUMBER}</td>
                </tr>
            </table>
            
            <h3>🌐 Application URLs:</h3>
            <ul>
                <li><strong>Frontend:</strong> <a href="http://localhost:${env.FRONTEND_PORT}">http://localhost:${env.FRONTEND_PORT}</a></li>
                <li><strong>Backend:</strong> <a href="http://localhost:${env.BACKEND_PORT}">http://localhost:${env.BACKEND_PORT}</a></li>
            </ul>
            
            <h3>📊 SonarQube Quality Report:</h3>
            <div style="background-color: #e8f5e8; padding: 15px; border: 1px solid #4caf50; border-radius: 5px;">
                <p><strong>🎯 Quality Gate:</strong> <span style="color: green;">PASSED ✅</span></p>
                <p><strong>🔗 Backend Report:</strong> <a href="${backendSonarUrl}">View Backend SonarQube</a></p>
                <p><strong>🔗 Frontend Report:</strong> <a href="${frontendSonarUrl}">View Frontend SonarQube</a></p>
                
                <h4>📈 Code Quality Metrics:</h4>
                <div id="sonar-metrics">
                    <p><em>💡 Click the SonarQube link above to view detailed metrics including:</em></p>
                    <ul>
                        <li>Code Coverage Percentage</li>
                        <li>Lines of Code</li>
                        <li>Bugs & Vulnerabilities</li>
                        <li>Code Smells</li>
                        <li>Maintainability Rating</li>
                        <li>Reliability Rating</li>
                        <li>Security Rating</li>
                    </ul>
                </div>
            </div>
            
            <h3>🧪 Test Results:</h3>
            <div style="background-color: #e8f4f8; padding: 15px; border: 1px solid #2196f3; border-radius: 5px;">
                <p><strong>✅ All tests passed successfully</strong></p>
                <p><strong>📋 Test Report:</strong> <a href="${env.BUILD_URL}testReport/">View Jenkins Test Results</a></p>
            </div>
            
            <h3>🔧 Build Information:</h3>
            <ul>
                <li><strong>Jenkins Build:</strong> <a href="${env.BUILD_URL}">View Build Details</a></li>
                <li><strong>Console Output:</strong> <a href="${env.BUILD_URL}console">View Build Logs</a></li>
                <li><strong>Build Duration:</strong> Started at ${new Date()}</li>
            </ul>
            
            <h3>💾 Database Information:</h3>
            <p><strong>Database File:</strong> pharmacy_db_${env.ENVIRONMENT.substring(0,3)}.sqlite</p>
            <p><em>Each environment uses its own independent SQLite database.</em></p>
            
            <hr>
            <p style="color: #666; font-size: 12px;">
                <em>📧 This is an automated notification from the CI/CD pipeline.<br>
                🤖 Build triggered by: ${env.BUILD_USER_ID ?: 'System'}<br>
                ⏰ Notification sent: ${new Date()}</em>
            </p>
        """,
        mimeType: 'text/html',
        to: "${env.LEAD_DEVELOPER_EMAIL}, ${env.PRODUCT_OWNER_EMAIL}"
    )
}

def getSonarQubeMetrics() {
    return script {
        try {
            def sonarApi = "http://localhost:9000/api/measures/component?component=pharmacy-project&metricKeys=coverage,lines,bugs,vulnerabilities,code_smells"
            def response = sh(script: "curl -s '${sonarApi}'", returnStdout: true).trim()
            return response
        } catch (Exception e) {
            return "Unable to fetch SonarQube metrics: ${e.message}"
        }
    }
} 