# Jenkins CI/CD Pipeline — Declarative Pipeline with Shared Library

A practical demonstration of a **Jenkins Declarative Pipeline** that builds, tests, and deploys a Dockerized Django application using **Jenkins Shared Libraries**.

---

## 📌 What This Pipeline Does

| Stage                 | Purpose                                                                       |
| --------------------- | ----------------------------------------------------------------------------- |
| **Hello**             | Runs a greeting function from the shared library (verifies library is loaded) |
| **Code**              | Clones the application repo from GitHub using a shared library function       |
| **Build**             | Builds the Docker image for the application                                   |
| **Push to DockerHub** | Pushes the built image to DockerHub                                           |
| **Test**              | Runs the container locally on port `8000` to verify it works                  |
| **Deploy**            | Placeholder for actual deployment (e.g., to a server or Kubernetes)           |

---

## 📂 Jenkinsfile Breakdown

```groovy
@Library("shared") _          // Load the shared library named "shared"
pipeline {
    agent { label 'knox' }    // Run on any agent with the label 'knox'

    stages {
        stage("Hello") { ... }            // Calls hello() from shared lib
        stage('Code') { ... }             // Calls clone() from shared lib
        stage('Build') { ... }            // Calls docker_build() from shared lib
        stage('Push to DockerHub') { ... } // Calls docker_push() from shared lib
        stage('Test') { ... }             // Runs the container for testing
        stage('Deploy') { ... }           // Deployment placeholder
    }
}
```

### Key Concepts Used

#### 1. `@Library("shared") _` — Shared Library Import

- Loads a globally configured Jenkins Shared Library named `shared`.
- The `_` (underscore) is required when no specific class is imported — it tells Groovy to load the entire library.

#### 2. `agent { label 'knox' }` — Agent Label

- The pipeline runs on a Jenkins agent (node/slave) that has the label `knox`.
- This is useful when you have multiple agents and want specific jobs to run on specific machines (e.g., a machine with Docker installed).

#### 3. `script { }` Block

- Used inside `steps` to write **scripted pipeline** code within a **declarative pipeline**.
- This allows you to call shared library functions like `hello()`, `clone()`, `docker_build()`, etc.

#### 4. Docker Commands

- `docker build` — Creates a Docker image from the application code.
- `docker push` — Uploads the image to DockerHub.
- `docker run` — Runs a test container to verify the image works.
- `docker stop / docker rm` — Cleans up any existing test container before running a new one.

#### 5. `|| true` — Error Suppression

```bash
docker stop notes-test || true
docker rm notes-test || true
```

- If the container doesn't exist, `docker stop` or `docker rm` would fail and break the pipeline.
- `|| true` ensures the command always returns success, even if it fails — preventing the pipeline from crashing.

---

## 📚 Jenkins Shared Library — Complete Guide

### What is a Shared Library?

A **Shared Library** is a way to write **reusable pipeline code** in a separate Git repository and share it across multiple Jenkins pipelines. Instead of copying the same code into every Jenkinsfile, you write it once in the library and call it wherever needed.

### Why Use Shared Libraries?

| Problem                                 | Solution with Shared Library                        |
| --------------------------------------- | --------------------------------------------------- |
| Same code repeated in many Jenkinsfiles | Write once, call everywhere                         |
| Hard to maintain pipeline code          | Update the library — all pipelines get the update   |
| Pipelines become long and messy         | Keep Jenkinsfile clean — logic lives in the library |
| No code reuse across teams              | Share the library org-wide                          |

### Shared Library Folder Structure

```
shared-library-repo/
├── vars/                    ← Global functions (called directly in Jenkinsfile)
│   ├── hello.groovy         ← hello() function
│   ├── clone.groovy         ← clone() function
│   ├── docker_build.groovy  ← docker_build() function
│   └── docker_push.groovy   ← docker_push() function
├── src/                     ← (Optional) Complex Groovy classes
│   └── org/
│       └── example/
│           └── Utils.groovy
├── resources/               ← (Optional) Non-Groovy files (configs, scripts)
└── README.md
```

### How the `vars/` Folder Works

Each `.groovy` file in `vars/` defines a **global function** that can be called directly from any Jenkinsfile.

#### Example: `vars/hello.groovy`

```groovy
def call() {
    echo "Hello from Shared Library!"
}
```

#### Example: `vars/clone.groovy`

```groovy
def call(String url, String branch) {
    git url: url, branch: branch
}
```

#### Example: `vars/docker_build.groovy`

```groovy
def call(String appName, String tag, String dockerUser) {
    sh "docker build -t ${dockerUser}/${appName}:${tag} ."
}
```

#### Example: `vars/docker_push.groovy`

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

> **Important:** The function name is the **filename**. So `docker_build.groovy` is called as `docker_build()` in the Jenkinsfile. Every file must have a `def call()` method.

---

## 🛠️ Step-by-Step: Setting Up This Pipeline

### Step 1: Install Required Jenkins Plugins

Go to **Manage Jenkins → Plugins → Available Plugins** and install:

| Plugin                                | Purpose                                |
| ------------------------------------- | -------------------------------------- |
| **Pipeline**                          | Enables declarative/scripted pipelines |
| **Git**                               | Allows Jenkins to clone repos          |
| **Docker Pipeline**                   | Docker integration with Jenkins        |
| **Pipeline: Shared Groovy Libraries** | Enables shared library support         |

### Step 2: Set Up Shared Library in Jenkins

1. Go to **Manage Jenkins → System** (or Configure System)
2. Scroll down to **Global Pipeline Libraries**
3. Click **Add** and fill in:

| Field                  | Value                                                     |
| ---------------------- | --------------------------------------------------------- |
| **Name**               | `shared` (must match `@Library("shared")` in Jenkinsfile) |
| **Default Version**    | `main` (or the branch name of your library repo)          |
| **Retrieval Method**   | Modern SCM → Git                                          |
| **Project Repository** | URL of your shared library Git repo                       |
| **Credentials**        | Add if it's a private repo                                |

4. Click **Save**

### Step 3: Add DockerHub Credentials

1. Go to **Manage Jenkins → Credentials → (global) → Add Credentials**
2. Select **Username with password**:

| Field        | Value                                                     |
| ------------ | --------------------------------------------------------- |
| **Username** | Your DockerHub username                                   |
| **Password** | Your DockerHub password or access token                   |
| **ID**       | `dockerhub-creds` (this ID is used in the shared library) |

3. Click **Save**

### Step 4: Set Up a Jenkins Agent (Node)

1. Go to **Manage Jenkins → Nodes → New Node**
2. Create a node with the label `knox`
3. Configure the agent with:
   - **Remote root directory** (e.g., `/home/ubuntu/jenkins-agent`)
   - **Launch method** (e.g., SSH, JNLP)
   - Make sure **Docker** is installed on this agent machine

> **Tip:** If using a single Jenkins server, you can also add the label `knox` to the built-in node under **Manage Jenkins → Nodes → Built-In Node → Configure**.

### Step 5: Create the Pipeline Job

1. Go to **Jenkins Dashboard → New Item**
2. Enter a name (e.g., `notes-app-pipeline`)
3. Select **Pipeline** and click OK
4. Under **Pipeline** section:

| Option             | Value                                       |
| ------------------ | ------------------------------------------- |
| **Definition**     | Pipeline script from SCM                    |
| **SCM**            | Git                                         |
| **Repository URL** | URL of the repo containing this Jenkinsfile |
| **Branch**         | `*/main`                                    |
| **Script Path**    | `Jenkinsfile`                               |

5. Click **Save**

### Step 6: Run the Pipeline

1. Click **Build Now**
2. Watch the pipeline execute in the **Stage View** or **Blue Ocean** UI
3. Check the console output for logs

---

## 🔄 Pipeline Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌───────────┐    ┌──────────┐    ┌──────────┐
│  Hello   │───▶│   Code   │───▶│  Build   │───▶│   Push    │───▶│  Test    │───▶│  Deploy  │
│ (Shared  │    │ (Clone   │    │ (Docker  │    │ (DockerHub│    │ (Run     │    │ (Deploy  │
│  Lib)    │    │  Repo)   │    │  Build)  │    │  Push)    │    │  & Test) │    │  Step)   │
└──────────┘    └──────────┘    └──────────┘    └───────────┘    └──────────┘    └──────────┘
```

---

## 📖 Quick Reference — Declarative vs Scripted Pipeline

| Feature            | Declarative Pipeline        | Scripted Pipeline         |
| ------------------ | --------------------------- | ------------------------- |
| **Syntax**         | Structured (`pipeline { }`) | Flexible (`node { }`)     |
| **Ease of Use**    | Beginner-friendly           | Requires Groovy knowledge |
| **Error Handling** | Built-in `post { }` blocks  | Manual `try-catch`        |
| **Shared Library** | Works with `@Library`       | Works with `@Library`     |
| **Recommended**    | ✅ Yes (Jenkins default)    | For advanced use cases    |

### Declarative Pipeline Skeleton

```groovy
@Library("library-name") _
pipeline {
    agent { label 'agent-label' }

    environment {
        MY_VAR = 'value'             // Set environment variables
    }

    stages {
        stage('Stage Name') {
            steps {
                echo "Hello"         // Simple step
                script {
                    // Scripted code or shared lib calls
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished" // Always runs
        }
        success {
            echo "Pipeline passed"   // Runs only on success
        }
        failure {
            echo "Pipeline failed"   // Runs only on failure
        }
    }
}
```

---

## 🔗 Useful Commands

```bash
# Check if Docker is installed on the agent
docker --version

# Check running containers
docker ps

# Check all containers (including stopped)
docker ps -a

# Remove all stopped containers
docker container prune -f

# View Jenkins logs (Linux)
sudo cat /var/log/jenkins/jenkins.log
```

---

## 📁 Repository Structure

```
Jenkins/
├── Jenkinsfile       ← Main pipeline definition
└── README.md         ← This file
```

---

## ⚠️ Common Errors & Fixes

| Error                         | Cause                            | Fix                                           |
| ----------------------------- | -------------------------------- | --------------------------------------------- |
| `No such DSL method 'hello'`  | Shared library not configured    | Set up library in **Manage Jenkins → System** |
| `docker: command not found`   | Docker not installed on agent    | Install Docker on the agent machine           |
| `permission denied` on Docker | Jenkins user not in docker group | Run `sudo usermod -aG docker jenkins`         |
| `could not resolve host`      | Agent has no internet access     | Check DNS / network on the agent              |
| `credentialsId not found`     | DockerHub creds not added        | Add credentials with correct ID in Jenkins    |

---
