# Spring Boot CI/CD demo

A minimal Spring Boot app plus a Jenkins pipeline, built for learning CI/CD hands-on.
The pipeline does: **Checkout → Build → Test → Package → Docker build → Deploy**.

## What's in here

- `src/` — a Spring Boot app with one REST endpoint (`/api/hello`) and a JUnit test
- `pom.xml` — Maven build, Java 17, Spring Boot 3.3.4
- `Dockerfile` — multi-stage build that produces a small runtime image
- `Jenkinsfile` — the pipeline definition, as code
- `docker-compose-jenkins.yml` — spins up Jenkins itself locally

You don't need Java or Maven installed on your own machine to try this — everything
runs inside containers. Having them locally is only useful if you want to run/test
the app directly with `mvn spring-boot:run` before wiring up Jenkins.

**Prerequisite: Docker Desktop** installed and running. That's the only hard requirement.

---

## 1. Get this project onto your own GitHub

Jenkins needs a real repo to pull from.

```bash
cd spring-boot-cicd-demo
git init
git add .
git commit -m "Initial commit: Spring Boot app + Jenkins pipeline"
```

Create an empty repo on GitHub (no README/license, since you already have files), then:

```bash
git remote add origin https://github.com/<your-username>/<your-repo>.git
git branch -M main
git push -u origin main
```

## 2. Start Jenkins

```bash
docker compose -f docker-compose-jenkins.yml up -d
```

Wait ~30 seconds, then open **http://localhost:8080**. Jenkins will ask for an initial
admin password. Grab it with:

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Paste it in, choose **"Install suggested plugins"**, then create your admin user.

## 3. Configure the build tools Jenkins needs

The `Jenkinsfile` references two named tools — you have to create them with these
exact names or the pipeline will fail with a "tool not found" error.

Go to **Manage Jenkins → Tools**:

- Under **Maven installations**, add one named `Maven-3`, check "Install automatically",
  pick any recent 3.9.x version.
- Under **JDK installations**, add one named `JDK-17`, check "Install automatically"
  (this needs the Eclipse Temurin installer plugin, which comes with the suggested
  plugin set).

> **If JDK auto-install doesn't work:** the `jenkins/jenkins:lts-jdk17` image already
> has Java 17 on its PATH. Just delete the `jdk 'JDK-17'` line from the `Jenkinsfile`
> and keep only the `maven` line.

## 4. Create the pipeline job

- **New Item → name it → Pipeline → OK**
- Under **Pipeline**, set "Definition" to **Pipeline script from SCM**
- SCM: **Git**, paste your repo URL, branch `*/main`
- Script Path: `Jenkinsfile` (already the default)
- Save, then click **Build Now**

Watch the stage view light up as it checks out, builds, and tests your code. Click
into a build's **Console Output** to see the raw Maven logs — this is where you'll
debug things when a stage fails.

## 5. Break something on purpose

This is the real "aha" moment for CI. Edit `HelloControllerTest.java` so an assertion
is wrong, push it, and re-run the build. Watch the **Test** stage fail and the whole
pipeline stop — nothing broken gets to Package or Deploy. Fix it, push again, watch
it go green. That's the entire point of CI in one exercise.

## 6. Enabling the Docker build/deploy stages

The last two stages run `docker build` and `docker run` *from inside Jenkins*, so the
Jenkins container needs the Docker CLI and access to the host's Docker daemon. The
compose file already mounts `/var/run/docker.sock` for you — you just need the CLI
binary inside the container:

```bash
docker exec -u root jenkins sh -c "apt-get update && apt-get install -y docker.io"
```

Re-run the pipeline. The `Docker build` stage will now build an image tagged with
the current build number, and `Deploy` will run it, exposing your app at
**http://localhost:8081/api/hello**.

If you'd rather not deal with this yet, just delete the `Docker build` and `Deploy`
stages from the `Jenkinsfile` and get comfortable with Build → Test → Package first.
That's still a real, working CI pipeline.

## 7. Trigger builds automatically on push

A webhook needs GitHub to be able to reach your Jenkins — but `localhost:8080` isn't
reachable from the internet. Two options:

- **Easiest for local practice:** in the pipeline job's config, under
  **Build Triggers**, check **"Poll SCM"** and set the schedule to `* * * * *`
  (checks for new commits every minute — good enough for learning, not for production).
- **Closer to a real setup:** expose Jenkins with `ngrok http 8080`, then add a
  webhook in your GitHub repo's Settings → Webhooks pointing at
  `https://<your-ngrok-url>/github-webhook/`, and enable
  **"GitHub hook trigger for GITScm polling"** in the job config instead of polling.

---

## Suggested next steps once this works

- Add a second, deliberately slower test and watch how it affects total pipeline time
- Add a `Dockerfile` healthcheck and a `docker ps` check as part of Deploy
- Try the same pipeline logic as a GitHub Actions workflow (`.github/workflows/ci.yml`)
  and compare how much of the syntax carries over
- Once comfortable, look at deploying the Docker image to a real target instead of
  the same machine (Render, Railway, or an AWS EC2 box)
