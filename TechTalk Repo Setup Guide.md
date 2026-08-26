# 🚀 TechTalk — Quick Git Guide

Repo: **`https://github.com/jeshu05/TechTalk.git`**

---

## 1. Initial Setup (One-Time)

### Step 1: Accept the GitHub Invite
- Check your email or GitHub notifications and accept the collaborator invitation.

### Step 2: Clone the Repository
Open your terminal and run:
```bash
git clone https://github.com/jeshu05/TechTalk.git
cd TechTalk
```

### Step 3: Create Your Own Branch
Instead of working directly on `main`, create a branch named after you or some name you like once: 

```bash
git checkout -b <branch-name>
git push -u origin <branch-name>
```

---

## 2. Daily Workflow (The 4 Commands You'll Use)

Whenever you make changes to the code:

```bash
# 1. Check what changed
git status

# 2. Stage all changes
git add .

# 3. Save your work with a message
git commit -m "Brief description of what you did"

# 4. Push to your branch
git push origin branch-name
```

---

## 3. Staying in Sync with the Team

Before starting new work or pushing, pull the latest updates from `main`:

```bash
# Get the latest team updates
git pull origin main

# (Optional) Merge latest main into your branch to prevent conflicts
git merge main
```

---

## 4. Cheat Sheet

| What you want to do | Command |
|---|---|
| Switch to your branch | `git checkout <your-name>` |
| Pull latest code | `git pull origin main` |
| Save your work | `git add . && git commit -m "your message"` |
| Push to GitHub | `git push origin <your-name>` |
| Check current branch/status | `git status` |

---

## 💡 Troubleshooting: Git asking for a Password?

GitHub does **not** accept your regular account password over HTTPS. If prompted:

1. Go to: **GitHub → Settings → Developer Settings → Personal Access Tokens → Tokens (classic)**
2. Click **Generate new token (classic)**, check the **`repo`** box, and click Generate.
3. Copy the token and paste it as your password in the terminal.