# Jenkins Shared Libraries — Function Reference

This folder contains all the **reusable Groovy functions** used across Jenkins Declarative Pipelines. Each `.groovy` file represents a single callable function — the filename is the function name.

> **Setup:** These files go inside the `vars/` directory of your Jenkins Shared Library Git repository. See the [Shared Libraries Learning Guide](../Jenkins-learnings/04-shared-libraries.md) for full setup instructions.

---

## 📋 Quick Reference

| #   | Function                   | File                                                             | Category    | Purpose                                               |
| --- | -------------------------- | ---------------------------------------------------------------- | ----------- | ----------------------------------------------------- |
| 1   | `hello()`                  | [hello.groovy](./hello.groovy)                                   | 🟢 Basic    | Test if the shared library is loaded                  |
| 2   | `clone()`                  | [clone.groovy](./clone.groovy)                                   | 🟢 Basic    | Clone a Git repo with URL and branch                  |
| 3   | `code_checkout()`          | [code_checkout.groovy](./code_checkout.groovy)                   | 🟢 Basic    | Lightweight Git checkout (no logs)                    |
| 4   | `clean_ws()`               | [clean_ws.groovy](./clean_ws.groovy)                             | 🟢 Basic    | Clean the Jenkins workspace after a build             |
| 5   | `docker_build()`           | [docker_build.groovy](./docker_build.groovy)                     | 🐳 Docker   | Build a Docker image with user/project:tag format     |
| 6   | `docker_push()`            | [docker_push.groovy](./docker_push.groovy)                       | 🐳 Docker   | Login to DockerHub and push the image                 |
| 7   | `docker_cleanup()`         | [docker_cleanup.groovy](./docker_cleanup.groovy)                 | 🐳 Docker   | Remove a Docker image after push                      |
| 8   | `docker_compose()`         | [docker_compose.groovy](./docker_compose.groovy)                 | 🐳 Docker   | Restart services using docker-compose                 |
| 9   | `trivy_scan()`             | [trivy_scan.groovy](./trivy_scan.groovy)                         | 🔒 Security | Scan filesystem for vulnerabilities using Trivy       |
| 10  | `owasp_dependency()`       | [owasp_dependency.groovy](./owasp_dependency.groovy)             | 🔒 Security | Run OWASP Dependency-Check for known CVEs             |
| 11  | `sonarqube_analysis()`     | [sonarqube_analysis.groovy](./sonarqube_analysis.groovy)         | 🔒 Security | Run SonarQube code analysis                           |
| 12  | `sonarqube_code_quality()` | [sonarqube_code_quality.groovy](./sonarqube_code_quality.groovy) | 🔒 Security | Wait for SonarQube Quality Gate result                |
| 13  | `run_tests()`              | [run_tests.groovy](./run_tests.groovy)                           | 🧪 Testing  | Placeholder for running unit tests                    |
| 14  | `generate_reports()`       | [generate_reports.groovy](./generate_reports.groovy)             | 📊 Reports  | Generate and archive build reports                    |
| 15  | `update_k8s_manifests()`   | [update_k8s_manifests.groovy](./update_k8s_manifests.groovy)     | ☸️ K8s      | Update Kubernetes deployment YAML with new image tags |

---

## 🟢 Basic Functions

### `hello()`

Verifies the shared library is loaded and the pipeline is working.

```groovy
// Usage
hello()
```

```groovy
// Source
def call(){
  echo "Hello Guys !"
  echo "checking ince again is the pipeline is working or not"
}
```

---

### `clone(url, branch)`

Clones a Git repository into the workspace.

| Parameter | Type   | Description          |
| --------- | ------ | -------------------- |
| `url`     | String | Git repository URL   |
| `branch`  | String | Branch name to clone |

```groovy
// Usage
clone("https://github.com/Rupesh-knox/django-notes-app.git", "main")
```

```groovy
// Source
def call(String url, String branch){
  echo "Clonning the branch"
  git url: "${url}", branch: "${branch}"
  echo "Code clonning successful"
}
```

---

### `code_checkout(GitUrl, GitBranch)`

A lightweight version of `clone()` — directly checks out code without extra logs.

| Parameter   | Type   | Description        |
| ----------- | ------ | ------------------ |
| `GitUrl`    | String | Git repository URL |
| `GitBranch` | String | Branch name        |

```groovy
// Usage
code_checkout("https://github.com/user/repo.git", "main")
```

```groovy
// Source
def call(String GitUrl, String GitBranch){
  git url: "${GitUrl}", branch: "${GitBranch}"
}
```

---

### `clean_ws()`

Cleans the entire Jenkins workspace. Use this in the `post` block to free disk space.

```groovy
// Usage (typically in post → always block)
clean_ws()
```

```groovy
// Source
def call() {
    echo "Cleaning up workspace..."
    cleanWs()
}
```

---

## 🐳 Docker Functions

### `docker_build(ProjectName, ImageTag, DockerHubUser)`

Builds a Docker image tagged as `user/project:tag`.

| Parameter       | Type   | Example       |
| --------------- | ------ | ------------- |
| `ProjectName`   | String | `"notes-app"` |
| `ImageTag`      | String | `"latest"`    |
| `DockerHubUser` | String | `"rupeshs11"` |

```groovy
// Usage
docker_build("notes-app", "latest", "rupeshs11")
// Runs: docker build -t rupeshs11/notes-app:latest .
```

```groovy
// Source
def call(String ProjectName, String ImageTag, String DockerHubUser){
  sh "docker build -t ${DockerHubUser}/${ProjectName}:${ImageTag} ."
}
```

---

### `docker_push(Project, ImageTag, dockerhubuser)`

Logs in to DockerHub using Jenkins credentials and pushes the image.

| Parameter       | Type   | Example       |
| --------------- | ------ | ------------- |
| `Project`       | String | `"notes-app"` |
| `ImageTag`      | String | `"latest"`    |
| `dockerhubuser` | String | `"rupeshs11"` |

> **Requires:** Jenkins credential with ID `dockerHubCredentials` (Username + Password)

```groovy
// Usage
docker_push("notes-app", "latest", "rupeshs11")
```

```groovy
// Source
def call(String Project, String ImageTag, String dockerhubuser){
  withCredentials([usernamePassword(
    credentialsId: 'dockerHubCredentials',
    passwordVariable: 'dockerHubPass',
    usernameVariable: 'dockerHubUser'
  )]) {
      sh "docker login -u ${dockerhubuser} -p ${dockerhubpass}"
  }
  sh "docker push ${dockerhubuser}/${Project}:${ImageTag}"
}
```

---

### `docker_cleanup(Project, ImageTag, DockerHubUser)`

Removes a local Docker image to free disk space on the agent.

| Parameter       | Type   | Example       |
| --------------- | ------ | ------------- |
| `Project`       | String | `"notes-app"` |
| `ImageTag`      | String | `"latest"`    |
| `DockerHubUser` | String | `"rupeshs11"` |

```groovy
// Usage
docker_cleanup("notes-app", "latest", "rupeshs11")
// Runs: docker rmi rupeshs11/notes-app:latest
```

---

### `docker_compose()`

Tears down and restarts services defined in `docker-compose.yml`.

```groovy
// Usage
docker_compose()
// Runs: docker-compose down && docker-compose up -d
```

---

## 🔒 Security & Code Quality Functions

### `trivy_scan()`

Runs a **Trivy** filesystem scan on the workspace and outputs results to `results.json`.

> **Requires:** Trivy installed on the Jenkins agent.

```groovy
// Usage
trivy_scan()
// Runs: trivy fs . -o results.json
```

---

### `owasp_dependency()`

Runs **OWASP Dependency-Check** to find known vulnerabilities (CVEs) in project dependencies.

> **Requires:** OWASP Dependency-Check plugin installed in Jenkins and configured as `OWASP` under tool installations.

```groovy
// Usage
owasp_dependency()
```

```groovy
// Source
def call(){
  dependencyCheck additionalArguments: '--scan ./', odcInstallation: 'OWASP'
  dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
}
```

---

### `sonarqube_analysis(SonarQubeAPI, Projectname, ProjectKey)`

Runs **SonarQube** static code analysis using the `sonar-scanner`.

| Parameter      | Type   | Description                                 |
| -------------- | ------ | ------------------------------------------- |
| `SonarQubeAPI` | String | SonarQube server name configured in Jenkins |
| `Projectname`  | String | Display name in SonarQube                   |
| `ProjectKey`   | String | Unique project key in SonarQube             |

> **Requires:** SonarQube plugin + SonarQube Scanner configured in Jenkins tools (`$SONAR_HOME`).

```groovy
// Usage
sonarqube_analysis("SonarQube", "notes-app", "notes-app-key")
```

```groovy
// Source
def call(String SonarQubeAPI, String Projectname, String ProjectKey){
  withSonarQubeEnv("${SonarQubeAPI}"){
      sh "$SONAR_HOME/bin/sonar-scanner -Dsonar.projectName=${Projectname} -Dsonar.projectKey=${ProjectKey} -X"
  }
}
```

---

### `sonarqube_code_quality()`

Waits for the **SonarQube Quality Gate** result (up to 1 minute). Use after `sonarqube_analysis()`.

```groovy
// Usage (call AFTER sonarqube_analysis)
sonarqube_code_quality()
```

```groovy
// Source
def call(){
  timeout(time: 1, unit: "MINUTES"){
      waitForQualityGate abortPipeline: false
  }
}
```

---

## 🧪 Testing & Reporting

### `run_tests()`

A placeholder function for running unit tests. Customize it based on your project's test framework.

```groovy
// Usage
run_tests()

// Customize for your project:
// sh "npm test"         → Node.js
// sh "mvn test"         → Java/Maven
// sh "python -m pytest" → Python
```

---

### `generate_reports(config)`

Generates a build report and archives it as a Jenkins artifact.

| Config Key    | Default     | Description                |
| ------------- | ----------- | -------------------------- |
| `projectName` | `'Project'` | Name for the report header |
| `imageName`   | `''`        | Docker image name          |
| `imageTag`    | `''`        | Docker image tag           |

```groovy
// Usage
generate_reports(
    projectName: 'Notes App',
    imageName: 'rupeshs11/notes-app',
    imageTag: 'latest'
)
```

---

## ☸️ Kubernetes

### `update_k8s_manifests(config)`

Updates Kubernetes deployment YAML files with a new image tag, commits, and pushes the changes — enabling **GitOps** workflows.

| Config Key       | Default                 | Description                        |
| ---------------- | ----------------------- | ---------------------------------- |
| `imageTag`       | _(required)_            | New image tag to set               |
| `manifestsPath`  | `'kubernetes'`          | Path to Kubernetes YAML files      |
| `gitCredentials` | `'github-credentials'`  | Jenkins credential ID for Git push |
| `gitUserName`    | `'Jenkins CI'`          | Git commit author name             |
| `gitUserEmail`   | `'jenkins@example.com'` | Git commit author email            |

```groovy
// Usage
update_k8s_manifests(
    imageTag: "build-${BUILD_NUMBER}",
    manifestsPath: 'kubernetes',
    gitCredentials: 'github-credentials'
)
```

> **What it does:**
>
> 1. Configures Git user
> 2. Uses `sed` to replace image tags in deployment YAMLs
> 3. Commits and pushes the updated manifests

---

## 🚀 Example: Full Pipeline Using These Libraries

```groovy
@Library("shared") _
pipeline {
    agent { label 'knox' }

    environment {
        DOCKER_USER = 'rupeshs11'
        APP = 'notes-app'
        TAG = "build-${BUILD_NUMBER}"
    }

    stages {
        stage('Hello')   { steps { script { hello() } } }
        stage('Code')    { steps { script { clone("https://github.com/user/repo.git", "main") } } }
        stage('Trivy')   { steps { script { trivy_scan() } } }
        stage('SonarQube') {
            steps { script { sonarqube_analysis("SonarQube", APP, "${APP}-key") } }
        }
        stage('Quality Gate') {
            steps { script { sonarqube_code_quality() } }
        }
        stage('OWASP')   { steps { script { owasp_dependency() } } }
        stage('Build')   { steps { script { docker_build(APP, TAG, DOCKER_USER) } } }
        stage('Push')    { steps { script { docker_push(APP, TAG, DOCKER_USER) } } }
        stage('Cleanup') { steps { script { docker_cleanup(APP, TAG, DOCKER_USER) } } }
        stage('Deploy')  { steps { echo "Deployed!" } }
    }

    post {
        always {
            script {
                generate_reports(projectName: APP, imageName: "${DOCKER_USER}/${APP}", imageTag: TAG)
                clean_ws()
            }
        }
    }
}
```

---

## ⚙️ Jenkins Prerequisites

| Requirement               | Where to Set Up                                               |
| ------------------------- | ------------------------------------------------------------- |
| Docker on agent           | Install Docker on the agent machine                           |
| DockerHub Credentials     | **Manage Jenkins → Credentials** → ID: `dockerHubCredentials` |
| SonarQube Server          | **Manage Jenkins → System → SonarQube servers**               |
| SonarQube Scanner         | **Manage Jenkins → Tools → SonarQube Scanner**                |
| OWASP Dependency-Check    | **Manage Jenkins → Tools → Dependency-Check** (name: `OWASP`) |
| Trivy                     | Install Trivy binary on the agent                             |
| Git Credentials (for K8s) | **Manage Jenkins → Credentials** → ID: `github-credentials`   |

---

## 📝 Author

**Rupesh** — Jenkins Shared Library Functions
