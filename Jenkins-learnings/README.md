# Jenkins Learnings 📚

A comprehensive collection of Jenkins concepts, configurations, and best practices — organized as individual guides for easy reference and revision.

---

## 📑 Topics

| #   | Topic                                                                         | Description                                                                            |
| --- | ----------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| 01  | [Freestyle Project](./01-freestyle-project.md)                                | GUI-based job setup, build steps, triggers, and environment variables                  |
| 02  | [Declarative Pipeline + GitHub Webhook](./02-declarative-pipeline-webhook.md) | Jenkinsfile syntax, stages, conditions, parallel builds, and auto-trigger via webhook  |
| 03  | [Jenkins Agents](./03-jenkins-agents.md)                                      | Master-agent architecture, SSH setup, labels, and Docker agents                        |
| 04  | [Shared Libraries](./04-shared-libraries.md)                                  | Reusable pipeline code, `vars/` functions, `src/` classes, and library configuration   |
| 05  | [User Management (RBAC)](./05-user-management-rbac.md)                        | Role-based access control, creating roles, assigning permissions, and lockout recovery |

---

## 🗺️ Learning Path

```
1. Freestyle Project        → Understand Jenkins basics using GUI
         ↓
2. Declarative Pipeline     → Write pipelines as code (Jenkinsfile)
         ↓
3. Agents                   → Distribute builds across multiple machines
         ↓
4. Shared Libraries         → Reuse pipeline code across all projects
         ↓
5. User Management (RBAC)   → Secure Jenkins with role-based access
```

---

## 🔗 Quick Links

### Freestyle Project

- [What is a Freestyle Project?](./01-freestyle-project.md#jenkins-freestyle-project)
- [Build Triggers (Poll SCM, Webhooks, Cron)](./01-freestyle-project.md#step-3-configure-build-triggers)
- [Environment Variables](./01-freestyle-project.md#environment-variables-in-freestyle)
- [Freestyle vs Pipeline Comparison](./01-freestyle-project.md#freestyle-vs-pipeline--quick-comparison)

### Declarative Pipeline

- [Pipeline Structure & All Blocks](./02-declarative-pipeline-webhook.md#declarative-pipeline-basics)
- [Agent Types](./02-declarative-pipeline-webhook.md#agent-types)
- [Conditional Stages (when)](./02-declarative-pipeline-webhook.md#conditions-and-when)
- [Parallel Stages](./02-declarative-pipeline-webhook.md#parallel-stages)
- [Parameters](./02-declarative-pipeline-webhook.md#parameters)
- [Post Actions](./02-declarative-pipeline-webhook.md#post-actions)
- [GitHub Webhook Setup](./02-declarative-pipeline-webhook.md#github-webhook-setup)

### Jenkins Agents

- [Architecture Diagram](./03-jenkins-agents.md#architecture-overview)
- [SSH Agent Setup (Step-by-Step)](./03-jenkins-agents.md#setting-up-a-permanent-agent-ssh)
- [Using Labels in Pipelines](./03-jenkins-agents.md#labels--how-they-work)
- [Docker as Agent](./03-jenkins-agents.md#docker-agent)

### Shared Libraries

- [Folder Structure](./04-shared-libraries.md#folder-structure)
- [Writing vars/ Functions](./04-shared-libraries.md#writing-functions-in-vars)
- [Using src/ Classes](./04-shared-libraries.md#using-classes-in-src)
- [Configuring in Jenkins](./04-shared-libraries.md#setting-up-shared-library-in-jenkins)
- [Loading Options (@Library)](./04-shared-libraries.md#loading-options)

### User Management

- [RBAC Setup Steps](./05-user-management-rbac.md#setting-up-rbac--step-by-step)
- [Creating & Assigning Roles](./05-user-management-rbac.md#managing-roles)
- [Common Role Configurations](./05-user-management-rbac.md#common-role-configurations)
- [Recovering from Lockout](./05-user-management-rbac.md#recovering-from-lockout)

---

## 📂 Folder Structure

```
Jenkins-learnings/
├── README.md                           ← This file (index)
├── info.txt                            ← Original topic list
├── 01-freestyle-project.md             ← Freestyle job guide
├── 02-declarative-pipeline-webhook.md  ← Pipeline + webhook guide
├── 03-jenkins-agents.md                ← Agents setup guide
├── 04-shared-libraries.md             ← Shared libraries guide
└── 05-user-management-rbac.md          ← RBAC guide
```

---

## ⚡ Cheat Sheet

| Task                     | Command / Location                                       |
| ------------------------ | -------------------------------------------------------- |
| Install Jenkins (Ubuntu) | `sudo apt install jenkins`                               |
| Start Jenkins            | `sudo systemctl start jenkins`                           |
| Get initial password     | `sudo cat /var/lib/jenkins/secrets/initialAdminPassword` |
| Jenkins URL              | `http://your-server-ip:8080`                             |
| Manage Plugins           | **Manage Jenkins → Plugins**                             |
| Add Credentials          | **Manage Jenkins → Credentials**                         |
| Configure Shared Library | **Manage Jenkins → System → Global Pipeline Libraries**  |
| Add Agent/Node           | **Manage Jenkins → Nodes → New Node**                    |
| Configure RBAC           | **Manage Jenkins → Manage and Assign Roles**             |
| View Pipeline Syntax     | `http://your-jenkins-url:8080/pipeline-syntax/`          |
| Restart Jenkins          | `http://your-jenkins-url:8080/restart`                   |
| Safe Restart             | `http://your-jenkins-url:8080/safeRestart`               |

---
