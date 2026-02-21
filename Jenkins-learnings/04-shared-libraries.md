# Jenkins Shared Libraries

A **Shared Library** is a collection of reusable Groovy scripts stored in a separate Git repository. It lets you write pipeline logic once and use it across all your Jenkins pipelines.

---

## Why Shared Libraries?

| Without Shared Library                     | With Shared Library                            |
| ------------------------------------------ | ---------------------------------------------- |
| Same code copy-pasted in every Jenkinsfile | Write once, call from any pipeline             |
| 10 Jenkinsfiles to update for one change   | Update the library — all pipelines get the fix |
| Long, messy Jenkinsfiles                   | Clean, short Jenkinsfiles                      |
| No code reuse across teams                 | Organization-wide reuse                        |

---

## Folder Structure

A shared library repo must follow this **exact structure**:

```
my-shared-library/         ← Git repository root
├── vars/                  ← ⭐ Global functions (most commonly used)
│   ├── hello.groovy
│   ├── clone.groovy
│   ├── docker_build.groovy
│   └── docker_push.groovy
├── src/                   ← (Optional) Groovy classes for complex logic
│   └── org/
│       └── devops/
│           └── Docker.groovy
├── resources/             ← (Optional) Non-Groovy files (configs, templates)
│   └── deploy-template.yaml
└── README.md
```

### What Goes Where?

| Folder       | Purpose                 | Called From Pipeline?                  |
| ------------ | ----------------------- | -------------------------------------- |
| `vars/`      | Simple global functions | ✅ Directly: `hello()`, `clone()`      |
| `src/`       | Complex Groovy classes  | Via import: `import org.devops.Docker` |
| `resources/` | Config files, templates | Via `libraryResource('file.yaml')`     |

---

## Writing Functions in `vars/`

Every `.groovy` file in `vars/` must have a `def call()` method. The **filename becomes the function name**.

### Example 1: `vars/hello.groovy`

```groovy
// Simple function — no parameters
def call() {
    echo "Hello from Shared Library!"
}
```

**Usage in Jenkinsfile:** `hello()`

### Example 2: `vars/clone.groovy`

```groovy
// Function with parameters
def call(String repoUrl, String branch) {
    echo "Cloning ${repoUrl} on branch ${branch}"
    git url: repoUrl, branch: branch
}
```

**Usage in Jenkinsfile:** `clone("https://github.com/user/repo.git", "main")`

### Example 3: `vars/docker_build.groovy`

```groovy
def call(String appName, String tag, String dockerUser) {
    echo "Building Docker image: ${dockerUser}/${appName}:${tag}"
    sh "docker build -t ${dockerUser}/${appName}:${tag} ."
}
```

**Usage in Jenkinsfile:** `docker_build("notes-app", "latest", "rupeshs11")`

### Example 4: `vars/docker_push.groovy`

```groovy
def call(String appName, String tag, String dockerUser) {
    withCredentials([usernamePassword(
        credentialsId: 'dockerhub-creds',
        usernameVariable: 'USER',
        passwordVariable: 'PASS'
    )]) {
        sh "docker login -u ${USER} -p ${PASS}"
        sh "docker push ${dockerUser}/${appName}:${tag}"
    }
}
```

**Usage in Jenkinsfile:** `docker_push("notes-app", "latest", "rupeshs11")`

### Example 5: `vars/notifySlack.groovy` (Advanced)

```groovy
def call(String status, String channel = '#devops') {
    def color = status == 'SUCCESS' ? 'good' : 'danger'
    slackSend(
        channel: channel,
        color: color,
        message: "${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${status}"
    )
}
```

**Usage:** `notifySlack("SUCCESS")` or `notifySlack("FAILURE", "#alerts")`

---

## Using Classes in `src/`

For more complex, object-oriented code:

### `src/org/devops/Docker.groovy`

```groovy
package org.devops

class Docker implements Serializable {
    def script    // Reference to the pipeline script

    Docker(script) {
        this.script = script
    }

    def build(String image, String tag) {
        script.sh "docker build -t ${image}:${tag} ."
    }

    def push(String image, String tag) {
        script.sh "docker push ${image}:${tag}"
    }
}
```

### Usage in Jenkinsfile:

```groovy
@Library('shared') _
import org.devops.Docker

pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                script {
                    def docker = new Docker(this)
                    docker.build('my-app', 'latest')
                    docker.push('my-app', 'latest')
                }
            }
        }
    }
}
```

---

## Using `resources/`

Store non-Groovy files like YAML templates, shell scripts, etc.

### `resources/deploy-template.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: __APP_NAME__
```

### Access in `vars/`:

```groovy
def call(String appName) {
    def template = libraryResource('deploy-template.yaml')
    def manifest = template.replace('__APP_NAME__', appName)
    writeFile file: 'deploy.yaml', text: manifest
    sh 'kubectl apply -f deploy.yaml'
}
```

---

## Setting Up Shared Library in Jenkins

### Step 1: Create the Library Repository

1. Create a new Git repo (e.g., `jenkins-shared-library`)
2. Add the `vars/` folder with your `.groovy` files
3. Push to GitHub/GitLab

### Step 2: Configure in Jenkins

1. Go to **Manage Jenkins → System** (Configure System)
2. Scroll to **Global Pipeline Libraries**
3. Click **Add** and fill in:

| Field                                      | Value                                                     |
| ------------------------------------------ | --------------------------------------------------------- |
| **Name**                                   | `shared`                                                  |
| **Default Version**                        | `main` (branch name)                                      |
| **Allow default version to be overridden** | ✅ Check this                                             |
| **Retrieval Method**                       | Modern SCM                                                |
| **Source Code Management**                 | Git                                                       |
| **Project Repository**                     | `https://github.com/your-user/jenkins-shared-library.git` |
| **Credentials**                            | Add if private repo                                       |

4. Click **Save**

### Step 3: Use in Jenkinsfile

```groovy
@Library("shared") _      // Load the library

pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                script {
                    hello()           // Calls vars/hello.groovy
                }
            }
        }
    }
}
```

---

## Loading Options

```groovy
// Load the default version (configured in Jenkins)
@Library("shared") _

// Load a specific branch
@Library("shared@develop") _

// Load a specific tag
@Library("shared@v1.0.0") _

// Load multiple libraries
@Library(["shared", "another-lib"]) _

// Load dynamically (inside a stage)
library "shared@main"
```

---

## Implicit vs Explicit Loading

| Type         | Syntax                                    | When to Use                               |
| ------------ | ----------------------------------------- | ----------------------------------------- |
| **Implicit** | Check "Load implicitly" in Jenkins config | Library auto-loads — no `@Library` needed |
| **Explicit** | `@Library("shared") _`                    | You control when/which version loads      |

> **Recommendation:** Use explicit loading (`@Library`) so your Jenkinsfile clearly shows its dependencies.

---

## Best Practices

1. **Keep functions small** — Each function should do one thing
2. **Use meaningful names** — `docker_build` is better than `build`
3. **Add parameters** — Make functions flexible with parameters
4. **Version your library** — Use Git tags (e.g., `v1.0.0`) for stable releases
5. **Test locally** — Use the Jenkins Pipeline Unit Testing Framework
6. **Document functions** — Add comments explaining parameters and usage
7. **Use credentials properly** — Never hardcode passwords; use `withCredentials()`

---

## Real-World Jenkinsfile Using Shared Library

This is exactly what our practical Jenkinsfile does:

```groovy
@Library("shared") _
pipeline {
    agent { label 'knox' }

    stages {
        stage("Hello") {
            steps {
                script { hello() }                    // vars/hello.groovy
            }
        }
        stage('Code') {
            steps {
                script {
                    clone("https://github.com/Rupesh-knox/django-notes-app.git", "main")
                }                                     // vars/clone.groovy
            }
        }
        stage('Build') {
            steps {
                script {
                    docker_build("notes-app", "latest", "rupeshs11")
                }                                     // vars/docker_build.groovy
            }
        }
        stage('Push to DockerHub') {
            steps {
                script {
                    docker_push("notes-app", "latest", "rupeshs11")
                }                                     // vars/docker_push.groovy
            }
        }
        stage('Test') {
            steps {
                sh """
                docker stop notes-test || true
                docker rm notes-test || true
                docker run -d -p 8000:8000 --name notes-test rupeshs11/notes-app:latest
                """
            }
        }
        stage('Deploy') {
            steps {
                echo "Deployment Successful"
            }
        }
    }
}
```

---

## Key Takeaways

- Shared Libraries = **reusable pipeline code** in a separate Git repo
- `vars/` folder holds **global functions** — filename = function name
- Every function needs a `def call()` method
- Configure the library in **Manage Jenkins → System → Global Pipeline Libraries**
- Load with `@Library("library-name") _` at the top of your Jenkinsfile
- The `_` underscore is **required** when not importing specific classes
- Use `src/` for complex classes and `resources/` for config files
