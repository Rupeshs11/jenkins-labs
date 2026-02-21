# Jenkins Agents (Master-Agent Architecture)

A **Jenkins Agent** (also called a **Node** or **Slave**) is a machine that executes jobs on behalf of the Jenkins master (controller). This allows you to distribute builds across multiple machines.

---

## Architecture Overview

```
┌────────────────────────┐
│    Jenkins Controller   │     ← Manages jobs, UI, scheduling
│      (Master Node)     │
└──────────┬─────────────┘
           │
     ┌─────┼─────────────────┐
     │     │                 │
     ▼     ▼                 ▼
┌────────┐ ┌────────┐ ┌────────┐
│ Agent 1│ │ Agent 2│ │ Agent 3│   ← Execute the actual builds
│ (Linux)│ │(Docker)│ │(Windows│
└────────┘ └────────┘ └────────┘
```

| Component               | Role                                            |
| ----------------------- | ----------------------------------------------- |
| **Controller (Master)** | Schedules jobs, serves the UI, manages agents   |
| **Agent (Slave/Node)**  | Executes the build steps assigned by the master |

---

## Why Use Agents?

| Reason                     | Explanation                                                  |
| -------------------------- | ------------------------------------------------------------ |
| **Distribute load**        | Don't overload the master with builds                        |
| **Different environments** | Run Linux builds on Linux, Windows builds on Windows         |
| **Security**               | Keep the master secure — agents do the heavy lifting         |
| **Scalability**            | Add more agents as your team grows                           |
| **Isolation**              | Each agent can have its own tools (Docker, Java, Node, etc.) |

---

## Agent Types

### 1. Permanent Agent (SSH-based)

A dedicated machine that stays connected to Jenkins.

### 2. Cloud Agent (Dynamic)

Spun up on demand (e.g., Docker containers, AWS EC2, Kubernetes pods) and destroyed after the build.

---

## Setting Up a Permanent Agent (SSH)

### Prerequisites

- A separate machine (VM, EC2 instance, etc.)
- Java installed on the agent machine
- SSH access from the master to the agent
- Jenkins master's public key added to the agent's `~/.ssh/authorized_keys`

### Step 1: Install Java on the Agent Machine

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk -y

# Verify
java -version
```

### Step 2: Create a Jenkins User on the Agent (Optional but Recommended)

```bash
sudo useradd -m -s /bin/bash jenkins
sudo su - jenkins
mkdir -p ~/jenkins-agent
```

### Step 3: Set Up SSH Key

**On the Jenkins Master:**

```bash
# Generate SSH key pair (if not already done)
ssh-keygen -t rsa -b 4096 -f ~/.ssh/jenkins-agent-key

# Copy the public key to the agent
ssh-copy-id -i ~/.ssh/jenkins-agent-key.pub jenkins@agent-ip-address
```

### Step 4: Add Credentials in Jenkins

1. Go to **Manage Jenkins → Credentials → (global) → Add Credentials**
2. Kind: **SSH Username with private key**

| Field       | Value                                          |
| ----------- | ---------------------------------------------- |
| Username    | `jenkins` (the user on the agent machine)      |
| Private Key | Enter directly → Paste the private key content |
| ID          | `agent-ssh-creds`                              |

### Step 5: Add the Agent Node in Jenkins

1. Go to **Manage Jenkins → Nodes → New Node**
2. Enter a name (e.g., `linux-agent-1`)
3. Select **Permanent Agent** → Click **OK**
4. Configure:

| Field                     | Value                                                       |
| ------------------------- | ----------------------------------------------------------- |
| **Name**                  | `linux-agent-1`                                             |
| **Remote root directory** | `/home/jenkins/jenkins-agent`                               |
| **Labels**                | `linux docker knox` (space-separated labels)                |
| **Usage**                 | "Only build jobs with label expressions matching this node" |
| **Launch method**         | Launch agents via SSH                                       |
| **Host**                  | IP address of the agent machine                             |
| **Credentials**           | Select the SSH credential you created                       |
| **Host Key Verification** | Non verifying (for testing) or Known hosts file             |

5. Click **Save**

### Step 6: Verify Connection

- Go to **Manage Jenkins → Nodes**
- The agent should show as **online** (connected)
- If offline, click the agent name and check the **log** for errors

---

## Using Agents in Pipelines

### Run on Any Agent

```groovy
pipeline {
    agent any    // Runs on any available agent (including master)
    stages {
        stage('Build') {
            steps {
                echo "Running on: ${env.NODE_NAME}"
            }
        }
    }
}
```

### Run on a Specific Labeled Agent

```groovy
pipeline {
    agent { label 'knox' }    // Runs only on agents with label 'knox'
    stages {
        stage('Build') {
            steps {
                echo "Running on agent: ${env.NODE_NAME}"
            }
        }
    }
}
```

### Different Agents per Stage

```groovy
pipeline {
    agent none    // No global agent

    stages {
        stage('Build on Linux') {
            agent { label 'linux' }
            steps {
                echo "Building on Linux"
            }
        }
        stage('Test on Docker') {
            agent {
                docker {
                    image 'node:18'
                    label 'docker'
                }
            }
            steps {
                sh 'node --version'
            }
        }
    }
}
```

---

## Labels — How They Work

Labels let you **tag agents** and **target specific agents** in your pipeline.

```
Agent: linux-agent-1  →  Labels: "linux docker knox"
Agent: windows-agent  →  Labels: "windows dotnet"
```

**In the pipeline:**

```groovy
agent { label 'linux' }           // Matches linux-agent-1
agent { label 'docker' }          // Matches linux-agent-1
agent { label 'knox' }            // Matches linux-agent-1
agent { label 'windows' }         // Matches windows-agent
agent { label 'linux && docker' } // Must have BOTH labels
agent { label 'linux || windows' }// Must have EITHER label
```

---

## Docker Agent

Run your pipeline inside a Docker container (requires Docker installed on the agent):

```groovy
pipeline {
    agent {
        docker {
            image 'python:3.11'
            args '-v /tmp:/tmp'     // Optional: mount volumes
        }
    }
    stages {
        stage('Test') {
            steps {
                sh 'python --version'
                sh 'pip install pytest && pytest'
            }
        }
    }
}
```

---

## Useful Agent Commands

```bash
# Check if Java is installed (required for agents)
java -version

# Check agent connectivity from master
ssh -i ~/.ssh/jenkins-agent-key jenkins@agent-ip

# Install Docker on the agent (Ubuntu)
sudo apt update
sudo apt install docker.io -y
sudo usermod -aG docker jenkins
sudo systemctl enable docker
sudo systemctl start docker

# Check agent disk space
df -h

# Check agent status via Jenkins CLI
java -jar jenkins-cli.jar -s http://localhost:8080/ list-nodes
```

---

## Troubleshooting

| Error                           | Cause                       | Fix                                                 |
| ------------------------------- | --------------------------- | --------------------------------------------------- |
| Agent offline                   | SSH connection failed       | Check SSH key, IP, firewall, and Java installation  |
| `java: command not found`       | Java not installed on agent | Install OpenJDK on the agent                        |
| `Permission denied (publickey)` | SSH key mismatch            | Re-copy the public key to agent's `authorized_keys` |
| `No agent available`            | No agent matches the label  | Check label in pipeline matches agent's label       |
| Agent connects then disconnects | Insufficient memory/disk    | Check resources on the agent machine                |

---

## Key Takeaways

- **Master** manages and schedules — **Agents** execute builds
- Use **labels** to target specific agents in your pipeline
- **SSH** is the most common launch method for permanent agents
- Each agent should have **Java** installed (required by Jenkins)
- Use `agent { label 'name' }` in your Jenkinsfile to control where stages run
- Install **Docker** on agents if your pipeline uses Docker commands
- For production: **never run builds on the master** — always use agents
