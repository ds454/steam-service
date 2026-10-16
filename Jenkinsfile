/* groovylint-disable CompileStatic, NestedBlockDepth */
pipeline {
  agent {
    kubernetes {
      yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          containers:
          - name: buildkit
            image: moby/buildkit:v0.31.0
            securityContext:
              privileged: true
          - name: maven
            image: maven:3.10.0-eclipse-temurin-25-alpine
            command:
            - sleep
            args:
            - infinity
      '''
    }
  }
  stages {
    stage('Test') {
      steps {
        container('maven') {
          script {
            sh """
              ls -lah
              mvn clean
              mvn clean compile
              mvn package
            """
          }
        }
      }
    }
    stage('Build') {
      environment {
        REGISTRY = "${env.GHCR_REGISTRY}"
        REPOSITORY = 'steam-service'
      }
      steps {
        container('buildkit') {
          script {
            String dockerConfig = '/tmp/registry-creds'
            String branch = env.BRANCH_NAME.replace('/', '-')
            String hash = env.GIT_COMMIT.take(10)

            withCredentials([usernamePassword(
              credentialsId: 'ghcr-creds',
              usernameVariable: 'USERNAME',
              passwordVariable: 'TOKEN'
            )]) {
              sh """
                export BUILDKIT_HTTP_TIMEOUT=300
                mkdir -p ${dockerConfig}

                AUTH=\$(printf '%s:%s' '${USERNAME}' '${TOKEN}' | base64 -w 0)
                echo '{"auths":{"${REGISTRY}":{"auth":"'\${AUTH}'"}}}' > ${dockerConfig}/config.json

                buildctl build \
                  --frontend dockerfile.v0 \
                  --local context=. \
                  --local dockerfile=. \
                  --output type=image,name=${REGISTRY}/${REPOSITORY}:latest,push=true \
                  --output type=image,name=${REGISTRY}/${REPOSITORY}:${branch}-latest,push=true \
                  --output type=image,name=${REGISTRY}/${REPOSITORY}:${hash},push=true \
                  --output type=image,name=${REGISTRY}/${REPOSITORY}:${branch}-${hash},push=true
              """
            }
          }
        }
      }
    }
  }
}
