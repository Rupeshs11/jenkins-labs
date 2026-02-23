# 🚀 Jenkins Complete Guide & Labs

Welcome to the **Jenkins DevOps Repository**! This repository serves as a comprehensive practical guide and learning resource for mastering Jenkins CI/CD — covering everything from basic Freestyle projects to advanced Declarative Pipelines, Shared Libraries, and User Management.

---

## 📂 Repository Structure

```
Jenkins/
├── 📚 Jenkins-learnings/        → Core Jenkins concepts & study guides
├── 🔧 shared-libraries/        → 15 reusable Groovy pipeline functions
├── 🛠️ praticals/                → Hands-on practical exercises
│   └── django-notesapp/         → Django Notes App CI/CD practical
├── 📜 Jenkinsfile               → Production-style Declarative Pipeline
└── 📖 README.md                 → This file
```

| Directory / File                                 | Description                                                      |
| ------------------------------------------------ | ---------------------------------------------------------------- |
| 📚 [**Jenkins-learnings/**](./Jenkins-learnings) | Detailed learning notes, concepts, and step-by-step guides       |
| 🔧 [**shared-libraries/**](./shared-libraries)   | 15 reusable Groovy functions for Docker, Security, K8s, and more |
| 🛠️ [**praticals/**](./praticals)                 | Hands-on practical exercises and projects                        |
| 📜 [**Jenkinsfile**](./Jenkinsfile)              | A complete Declarative Pipeline using Shared Libraries           |

---

## 📖 Jenkins Learnings (Study Guides)

Core Jenkins topics documented into separate, easy-to-read guides:

| #   | Topic                                                                                         | What You'll Learn                                                 |
| --- | --------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| 01  | [**Freestyle Project**](./Jenkins-learnings/01-freestyle-project.md)                          | GUI-based jobs, build steps, triggers, env variables              |
| 02  | [**Declarative Pipeline & Webhooks**](./Jenkins-learnings/02-declarative-pipeline-webhook.md) | Jenkinsfile syntax, stages, parallel builds, GitHub webhooks      |
| 03  | [**Jenkins Agents**](./Jenkins-learnings/03-jenkins-agents.md)                                | Master-slave architecture, SSH setup, labels, Docker agents       |
| 04  | [**Shared Libraries**](./Jenkins-learnings/04-shared-libraries.md)                            | Reusable `vars/` functions, `src/` classes, library configuration |
| 05  | [**User Management (RBAC)**](./Jenkins-learnings/05-user-management-rbac.md)                  | Role-based access control, permissions, lockout recovery          |

👉 **[View the Full Learning Index](./Jenkins-learnings/README.md)**

---

## 🔧 Shared Libraries (Reusable Functions)

A collection of **15 Groovy functions** that can be loaded into any Jenkins pipeline via `@Library("shared") _`. These go in the `vars/` directory of your shared library Git repo.

| Category          | Functions                                                                                                                                                                                                                                                                  | Purpose                                      |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------- |
| 🟢 **Basic**      | [`hello`](./shared-libraries/hello.groovy), [`clone`](./shared-libraries/clone.groovy), [`code_checkout`](./shared-libraries/code_checkout.groovy), [`clean_ws`](./shared-libraries/clean_ws.groovy)                                                                       | Greetings, Git clone, workspace cleanup      |
| 🐳 **Docker**     | [`docker_build`](./shared-libraries/docker_build.groovy), [`docker_push`](./shared-libraries/docker_push.groovy), [`docker_cleanup`](./shared-libraries/docker_cleanup.groovy), [`docker_compose`](./shared-libraries/docker_compose.groovy)                               | Build, push, clean images, compose services  |
| 🔒 **Security**   | [`trivy_scan`](./shared-libraries/trivy_scan.groovy), [`owasp_dependency`](./shared-libraries/owasp_dependency.groovy), [`sonarqube_analysis`](./shared-libraries/sonarqube_analysis.groovy), [`sonarqube_code_quality`](./shared-libraries/sonarqube_code_quality.groovy) | Vulnerability scanning & code quality        |
| 📊 **Reports**    | [`run_tests`](./shared-libraries/run_tests.groovy), [`generate_reports`](./shared-libraries/generate_reports.groovy)                                                                                                                                                       | Unit tests & build report archiving          |
| ☸️ **Kubernetes** | [`update_k8s_manifests`](./shared-libraries/update_k8s_manifests.groovy)                                                                                                                                                                                                   | GitOps — update K8s YAML with new image tags |

👉 **[View Full Function Reference & Usage](./shared-libraries/README.md)**

---

## 🚀 Main Pipeline Practical

The root `Jenkinsfile` demonstrates a real-world **Declarative Pipeline** that builds, tests, and deploys a Dockerized Django application using the shared library functions.

### Pipeline Stages

| Stage                 | Purpose                                 |
| --------------------- | --------------------------------------- |
| **Hello**             | Verifies the shared library is loaded   |
| **Code**              | Clones the application repo from GitHub |
| **Build**             | Builds a Docker image for the app       |
| **Push to DockerHub** | Authenticates and pushes the image      |
| **Test**              | Runs a test container on port `8000`    |
| **Deploy**            | Deployment placeholder                  |

```groovy
@Library("shared") _
pipeline {
    agent { label 'knox' }
    stages {
        stage("Hello") { ... }            // hello()
        stage('Code') { ... }             // clone()
        stage('Build') { ... }            // docker_build()
        stage('Push to DockerHub') { ... } // docker_push()
        stage('Test') { ... }             // docker run
        stage('Deploy') { ... }           // echo
    }
}
```

### How to Run

1. Set up your [**Shared Library repo**](./shared-libraries/README.md) with the `vars/` functions
2. Configure the library in **Manage Jenkins → System → Global Pipeline Libraries** (name: `shared`)
3. Create a **Pipeline Job** pointed to this repo's `Jenkinsfile`
4. Ensure your agent has the label `knox` and Docker installed
5. Add DockerHub credentials (ID: `dockerHubCredentials`)
6. Click **Build Now** 🚀

---


