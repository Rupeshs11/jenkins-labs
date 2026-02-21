# Declarative Pipeline + GitHub Webhook

A **Declarative Pipeline** is the modern, recommended way to define Jenkins CI/CD pipelines using code (Jenkinsfile). Combined with **GitHub Webhooks**, it enables fully automated builds triggered on every push.

---

## Declarative Pipeline Basics

### Structure

Every declarative pipeline follows this structure:

```groovy
pipeline {
    agent any                        // Where to run

    environment {                    // Environment variables
        APP_NAME = 'my-app'
    }

    stages {                         // Define your stages
        stage('Build') {
            steps {
                echo "Building ${APP_NAME}"
            }
        }
        stage('Test') {
            steps {
                echo "Testing..."
            }
        }
        stage('Deploy') {
            steps {
                echo "Deploying..."
            }
        }
    }

    post {                           // Post-build actions
        success {
            echo "Pipeline passed!"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}
```

### Key Blocks Explained

| Block           | Purpose                                  | Required?       |
| --------------- | ---------------------------------------- | --------------- |
| `pipeline { }`  | Root block — everything goes inside this | ✅ Yes          |
| `agent`         | Defines where the pipeline runs          | ✅ Yes          |
| `environment`   | Sets environment variables               | ❌ Optional     |
| `stages`        | Contains all the stages                  | ✅ Yes          |
| `stage('Name')` | A single phase of the pipeline           | ✅ At least one |
| `steps`         | The actual commands to run               | ✅ Yes          |
| `post`          | Actions after pipeline completes         | ❌ Optional     |

---

## Agent Types

```groovy
// Run on any available agent
agent any

// Run on a specific labeled agent
agent { label 'my-agent' }

// Run inside a Docker container
agent {
    docker {
        image 'node:18'
    }
}

// No global agent — define per stage
agent none
```

---

## Environment Variables

```groovy
environment {
    // Static values
    APP_NAME = 'my-app'
    DEPLOY_ENV = 'production'

    // From Jenkins credentials
    DOCKER_CREDS = credentials('dockerhub-creds')
    // This creates: DOCKER_CREDS_USR and DOCKER_CREDS_PSW
}
```

---

## Conditions and When

Run stages conditionally:

```groovy
stage('Deploy to Prod') {
    when {
        branch 'main'              // Only on main branch
    }
    steps {
        echo "Deploying to production"
    }
}

stage('Deploy to Staging') {
    when {
        not { branch 'main' }      // All branches except main
    }
    steps {
        echo "Deploying to staging"
    }
}
```

Other `when` conditions:

```groovy
when { branch 'main' }                          // Specific branch
when { environment name: 'DEPLOY', value: 'true' }  // Env var check
when { expression { return params.RUN_TESTS } } // Custom expression
when { changeset '**/*.js' }                     // Only if JS files changed
```

---

## Parameters

Make your pipeline interactive:

```groovy
pipeline {
    agent any

    parameters {
        string(name: 'BRANCH', defaultValue: 'main', description: 'Branch to build')
        choice(name: 'ENV', choices: ['dev', 'staging', 'prod'], description: 'Deploy environment')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run tests?')
    }

    stages {
        stage('Build') {
            steps {
                echo "Building branch: ${params.BRANCH}"
                echo "Environment: ${params.ENV}"
            }
        }
        stage('Test') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                echo "Running tests..."
            }
        }
    }
}
```

---

## Parallel Stages

Run stages in parallel to save time:

```groovy
stage('Tests') {
    parallel {
        stage('Unit Tests') {
            steps {
                echo "Running unit tests..."
            }
        }
        stage('Integration Tests') {
            steps {
                echo "Running integration tests..."
            }
        }
        stage('Lint') {
            steps {
                echo "Running linter..."
            }
        }
    }
}
```

---

## Post Actions

```groovy
post {
    always {
        echo "This always runs"
        // Clean up workspace
        cleanWs()
    }
    success {
        echo "Build succeeded!"
        // Send Slack notification, email, etc.
    }
    failure {
        echo "Build failed!"
    }
    unstable {
        echo "Build is unstable (e.g., test failures)"
    }
    changed {
        echo "Pipeline status changed from last run"
    }
}
```

---

## GitHub Webhook Setup

Webhooks allow GitHub to **automatically notify Jenkins** whenever code is pushed—no polling needed.

### Step 1: Install Plugin

- Go to **Manage Jenkins → Plugins → Available**
- Install **GitHub Integration Plugin** (if not already installed)

### Step 2: Configure Jenkins Job

1. Create a **Pipeline** job
2. Under **Build Triggers**, check: **GitHub hook trigger for GITScm polling**
3. Under **Pipeline**:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repo URL: `https://github.com/your-username/your-repo.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`

### Step 3: Create Webhook on GitHub

1. Go to your **GitHub repo → Settings → Webhooks → Add webhook**
2. Fill in:

| Field            | Value                                          |
| ---------------- | ---------------------------------------------- |
| **Payload URL**  | `http://your-jenkins-url:8080/github-webhook/` |
| **Content type** | `application/json`                             |
| **Secret**       | (optional, for security)                       |
| **Events**       | Select **Just the push event**                 |

3. Click **Add webhook**

> **Important:** Your Jenkins server must be **publicly accessible** for GitHub to send webhooks. If running locally, use **ngrok** to create a tunnel:
>
> ```bash
> ngrok http 8080
> ```
>
> Then use the ngrok URL as the Payload URL: `https://abc123.ngrok.io/github-webhook/`

### Step 4: Test the Webhook

1. Push a commit to your repo
2. Check **GitHub → Settings → Webhooks** — you should see a green checkmark ✅
3. Jenkins should automatically trigger the pipeline

---

## Webhook Troubleshooting

| Issue             | Cause                  | Fix                                                              |
| ----------------- | ---------------------- | ---------------------------------------------------------------- |
| Webhook shows ❌  | Jenkins not reachable  | Ensure Jenkins URL is public / use ngrok                         |
| 403 Forbidden     | CSRF protection        | Go to **Manage Jenkins → Security** → Enable proxy compatibility |
| Job not triggered | Trigger not enabled    | Check **GitHub hook trigger for GITScm polling** is checked      |
| Payload URL wrong | Missing trailing slash | URL must end with `/github-webhook/` (with the slash)            |

---

## Complete Example: Pipeline + Webhook

```groovy
pipeline {
    agent any

    environment {
        DOCKER_USER = 'rupeshs11'
        APP_NAME = 'notes-app'
        IMAGE_TAG = "build-${BUILD_NUMBER}"
    }

    stages {
        stage('Clone') {
            steps {
                git url: 'https://github.com/Rupesh-knox/django-notes-app.git',
                    branch: 'main'
            }
        }

        stage('Build') {
            steps {
                sh "docker build -t ${DOCKER_USER}/${APP_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'USER',
                    passwordVariable: 'PASS'
                )]) {
                    sh "docker login -u ${USER} -p ${PASS}"
                    sh "docker push ${DOCKER_USER}/${APP_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Deploy') {
            steps {
                sh """
                docker stop ${APP_NAME} || true
                docker rm ${APP_NAME} || true
                docker run -d -p 8000:8000 --name ${APP_NAME} ${DOCKER_USER}/${APP_NAME}:${IMAGE_TAG}
                """
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed — Image: ${DOCKER_USER}/${APP_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo "❌ Pipeline failed — Check console output"
        }
    }
}
```

---

## Key Takeaways

- **Declarative Pipeline** is the recommended way to write Jenkins pipelines
- Everything is defined in a **Jenkinsfile** stored in your repo (Infrastructure as Code)
- **GitHub Webhooks** trigger builds instantly on push — no polling delay
- Use `when` blocks for **conditional stage execution**
- Use `post` blocks for **cleanup, notifications, and error handling**
- Use `parallel` to **speed up** independent stages
