# User Management in Jenkins (Role-Based Access Control)

Jenkins supports **Role-Based Access Control (RBAC)** to manage who can do what. Using the **Role-Based Authorization Strategy** plugin, you can create roles and assign permissions to users.

---

## Why RBAC?

| Without RBAC                        | With RBAC                                      |
| ----------------------------------- | ---------------------------------------------- |
| Everyone has admin access           | Users only see and do what they're allowed to  |
| No control over who triggers builds | Restrict build/deploy to specific users        |
| Security risk                       | Follows the principle of least privilege       |
| Not suitable for teams              | Teams can have their own roles and permissions |

---

## Setting Up RBAC — Step by Step

### Step 1: Install the Plugin

1. Go to **Manage Jenkins → Plugins → Available Plugins**
2. Search for **Role-based Authorization Strategy**
3. Install and restart Jenkins

### Step 2: Enable Role-Based Strategy

1. Go to **Manage Jenkins → Security** (Configure Global Security)
2. Under **Authorization**, select: **Role-Based Strategy**
3. Click **Save**

> **⚠️ Warning:** Before switching, make sure your current admin user exists. Otherwise you'll lock yourself out!

### Step 3: Create Users

1. Go to **Manage Jenkins → Users → Create User**
2. Create users as needed:

| Username    | Role (we'll assign later)          |
| ----------- | ---------------------------------- |
| `admin`     | Full admin access                  |
| `developer` | Can view and build jobs            |
| `viewer`    | Read-only access                   |
| `deployer`  | Can build and deploy specific jobs |

---

## Managing Roles

Go to **Manage Jenkins → Manage and Assign Roles**

You'll see three options:

1. **Manage Roles** — Create and define roles with permissions
2. **Assign Roles** — Assign roles to users
3. **Role Strategy Macros** — Advanced pattern-based roles

---

### Managing Roles — Creating Roles

Go to **Manage and Assign Roles → Manage Roles**

#### Global Roles

These apply across all of Jenkins:

| Role Name   | Permissions                                         |
| ----------- | --------------------------------------------------- |
| `admin`     | ✅ All permissions (Overall/Administer)             |
| `developer` | ✅ Overall/Read, Job/Build, Job/Read, Job/Workspace |
| `viewer`    | ✅ Overall/Read, Job/Read                           |

**How to create:**

1. Under **Global Roles**, type a role name (e.g., `developer`)
2. Click **Add**
3. Check the appropriate permission checkboxes

#### Permission Categories

| Category        | Key Permissions                                           | Description            |
| --------------- | --------------------------------------------------------- | ---------------------- |
| **Overall**     | Administer, Read                                          | System-level access    |
| **Job**         | Build, Cancel, Configure, Create, Delete, Read, Workspace | Job-level actions      |
| **View**        | Configure, Create, Delete, Read                           | Dashboard views        |
| **SCM**         | Tag                                                       | Source code management |
| **Credentials** | Create, Delete, Update, View                              | Manage credentials     |
| **Agent**       | Build, Configure, Connect, Create, Delete                 | Manage nodes/agents    |

#### Project Roles (Item Roles)

These apply to specific jobs matching a **pattern**:

| Role Name      | Pattern       | Permissions                        |
| -------------- | ------------- | ---------------------------------- |
| `frontend-dev` | `frontend-.*` | Job/Build, Job/Read                |
| `backend-dev`  | `backend-.*`  | Job/Build, Job/Read, Job/Configure |
| `deploy-team`  | `.*-deploy`   | Job/Build, Job/Read                |

**Pattern examples:**

```
frontend-.*     → Matches: frontend-build, frontend-test, frontend-deploy
.*-deploy       → Matches: app-deploy, service-deploy
notes-.*        → Matches: notes-app-build, notes-app-deploy
.*              → Matches everything (use carefully!)
```

---

### Assigning Roles — Mapping Users to Roles

Go to **Manage and Assign Roles → Assign Roles**

#### Global Role Assignments

| User         | admin | developer | viewer |
| ------------ | ----- | --------- | ------ |
| `admin`      | ✅    |           |        |
| `developer1` |       | ✅        |        |
| `viewer1`    |       |           | ✅     |
| `deployer1`  |       | ✅        |        |

#### Project Role Assignments

| User        | frontend-dev | backend-dev | deploy-team |
| ----------- | ------------ | ----------- | ----------- |
| `dev-fe`    | ✅           |             |             |
| `dev-be`    |              | ✅          |             |
| `deployer1` |              |             | ✅          |

---

## Common Role Configurations

### Admin Role

```
✅ Overall/Administer (gives all permissions)
```

### Developer Role

```
✅ Overall/Read
✅ Job/Build
✅ Job/Cancel
✅ Job/Read
✅ Job/Workspace
✅ View/Read
```

### Viewer Role (Read-Only)

```
✅ Overall/Read
✅ Job/Read
✅ View/Read
```

### Deployer Role

```
✅ Overall/Read
✅ Job/Build
✅ Job/Read
✅ Job/Workspace
✅ Credentials/View
```

---

## Security Best Practices

1. **Never give everyone admin access** — Use the principle of least privilege
2. **Create service accounts** — For automated tools (not personal accounts)
3. **Use project roles** — Restrict access to specific jobs using patterns
4. **Enable CSRF protection** — **Manage Jenkins → Security → CSRF Protection**
5. **Enable agent → master security** — Prevents agents from accessing master
6. **Regular audits** — Review who has access periodically
7. **Use LDAP/Active Directory** — For large teams, integrate with your org's directory
8. **API Token management** — Users should use API tokens instead of passwords

---

## Matrix-Based Security (Alternative)

If you don't want to install a plugin, Jenkins has a built-in **Matrix-based security** option:

1. Go to **Manage Jenkins → Security**
2. Under **Authorization**, select **Matrix-based security**
3. Add users and check permissions individually

The drawback is that you manage permissions **per user**, not per role — which becomes difficult with many users.

---

## Recovering from Lockout

If you accidentally lock yourself out:

### Option 1: Edit config.xml

```bash
# Stop Jenkins
sudo systemctl stop jenkins

# Edit the config file
sudo nano /var/lib/jenkins/config.xml
```

Change:

```xml
<authorizationStrategy class="com.michelin.cio.hudson.plugins.rolestrategy.RoleBasedAuthorizationStrategy">
```

To:

```xml
<authorizationStrategy class="hudson.security.AuthorizationStrategy$Unsecured"/>
```

Also change:

```xml
<useSecurity>true</useSecurity>
```

To:

```xml
<useSecurity>false</useSecurity>
```

Then restart:

```bash
sudo systemctl start jenkins
```

> **Important:** Re-enable security immediately after logging in!

### Option 2: Use Jenkins CLI

```bash
java -jar jenkins-cli.jar -s http://localhost:8080/ groovy = <<< "jenkins.model.Jenkins.instance.authorizationStrategy = new hudson.security.AuthorizationStrategy.Unsecured()"
```

---

## Key Takeaways

- Install **Role-based Authorization Strategy** plugin for RBAC
- Create **Global Roles** for organization-wide permissions
- Create **Project Roles** with patterns to restrict access to specific jobs
- **Assign roles** to users — not individual permissions
- Always have at least one **admin user** before enabling RBAC
- Use the **principle of least privilege** — give only what's needed
- Know how to **recover from lockout** by editing `config.xml`
