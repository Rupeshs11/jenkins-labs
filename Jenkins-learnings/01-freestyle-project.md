# Jenkins Freestyle Project

A **Freestyle Project** is the simplest and most flexible job type in Jenkins. It provides a web-based GUI to configure build steps without writing any pipeline code.

---

## When to Use Freestyle

| Use Case                      | Recommended?            |
| ----------------------------- | ----------------------- |
| Quick one-off builds          | ✅ Yes                  |
| Simple shell script execution | ✅ Yes                  |
| Complex multi-stage CI/CD     | ❌ Use Pipeline instead |
| Version-controlled pipeline   | ❌ Use Pipeline instead |

---

## How to Create a Freestyle Project

### Step 1: Create the Job

1. Go to **Jenkins Dashboard → New Item**
2. Enter a name (e.g., `my-freestyle-job`)
3. Select **Freestyle project** → Click **OK**

### Step 2: Configure Source Code Management (SCM)

1. Under **Source Code Management**, select **Git**
2. Enter the **Repository URL**:
   ```
   https://github.com/your-username/your-repo.git
   ```
3. Set the **Branch**: `*/main`
4. Add **Credentials** if the repo is private

### Step 3: Configure Build Triggers

Choose when the job should run:

| Trigger                          | Description                                                              |
| -------------------------------- | ------------------------------------------------------------------------ |
| **Poll SCM**                     | Jenkins checks the repo at intervals (e.g., `H/5 * * * *` = every 5 min) |
| **GitHub hook trigger**          | Runs when GitHub sends a webhook (best option)                           |
| **Build periodically**           | Runs on a cron schedule regardless of changes                            |
| **Trigger from another project** | Runs after another job completes                                         |

#### Poll SCM Example (Cron Syntax)

```
H/5 * * * *    → Every 5 minutes
H * * * *      → Every hour
H 2 * * *      → Daily at 2 AM
H 2 * * 1-5    → Weekdays at 2 AM
```

### Step 4: Add Build Steps

Under **Build Steps**, click **Add build step → Execute shell**:

```bash
# Example: Build and run a Docker container
echo "Starting build..."
docker build -t my-app:latest .
docker run -d -p 8080:8080 --name my-app my-app:latest
echo "Build complete!"
```

You can add multiple build steps — they execute sequentially.

### Step 5: Add Post-Build Actions (Optional)

| Action                         | Purpose                               |
| ------------------------------ | ------------------------------------- |
| **Archive artifacts**          | Save build outputs (JARs, ZIPs, etc.) |
| **Publish JUnit test results** | Display test reports                  |
| **Email notification**         | Send build status emails              |
| **Trigger other projects**     | Chain multiple jobs together          |

### Step 6: Save and Build

1. Click **Save**
2. Click **Build Now** from the job page
3. Check the **Console Output** for logs

---

## Example: Freestyle Job for Docker Build

**Build Step (Execute Shell):**

```bash
#!/bin/bash

echo "=== Cloning Repository ==="
git clone https://github.com/your-username/your-app.git
cd your-app

echo "=== Building Docker Image ==="
docker build -t your-dockerhub/your-app:latest .

echo "=== Pushing to DockerHub ==="
docker login -u $DOCKER_USER -p $DOCKER_PASS
docker push your-dockerhub/your-app:latest

echo "=== Running Container ==="
docker stop my-app || true
docker rm my-app || true
docker run -d -p 8080:8080 --name my-app your-dockerhub/your-app:latest

echo "=== Done ==="
```

---

## Environment Variables in Freestyle

Jenkins provides built-in environment variables you can use in shell scripts:

| Variable        | Description                 |
| --------------- | --------------------------- |
| `$BUILD_NUMBER` | Current build number        |
| `$JOB_NAME`     | Name of the job             |
| `$WORKSPACE`    | Path to the job's workspace |
| `$BUILD_URL`    | URL to the current build    |
| `$GIT_COMMIT`   | Current Git commit hash     |
| `$GIT_BRANCH`   | Current Git branch          |

**Usage:**

```bash
echo "Building job: $JOB_NAME, Build #$BUILD_NUMBER"
echo "Commit: $GIT_COMMIT"
```

You can also add custom environment variables:

1. Go to **Job → Configure → Build Environment**
2. Check **Inject environment variables** (requires EnvInject plugin)
3. Add key-value pairs

---

## Freestyle vs Pipeline — Quick Comparison

| Feature            | Freestyle                       | Pipeline                            |
| ------------------ | ------------------------------- | ----------------------------------- |
| Configuration      | GUI-based (click and configure) | Code-based (Jenkinsfile)            |
| Version Control    | ❌ Not stored in repo           | ✅ Stored in repo as Jenkinsfile    |
| Complex Workflows  | Limited                         | Full support (parallel, conditions) |
| Shared Libraries   | ❌ Not supported                | ✅ Supported                        |
| Visualisation      | Basic                           | Stage View / Blue Ocean             |
| Restart from Stage | ❌ No                           | ✅ Yes                              |

> **Tip:** Use Freestyle for quick experiments and learning. Use Pipeline for real projects.

---

## Key Takeaways

- Freestyle is **GUI-driven** — no coding required
- Great for **beginners** and simple tasks
- Not ideal for production CI/CD (use Declarative Pipeline instead)
- All configuration lives inside Jenkins, **not in your repo**
- Use **Execute Shell** build step to run any Linux command
