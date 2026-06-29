#!/bin/bash

# ==============================================================================
# BACKEND DEPLOYMENT SCRIPT FOR OIPA SECURITY TOOL
# ==============================================================================
# This script automates building and deploying the Spring Boot backend jar to 
# the remote server.
# ==============================================================================

set -e

# --- CONFIGURATION SECTION ---
SERVER_IP="10.10.3.237"
SERVER_USER="atumverse"
SERVER_PORT="22"

# Local Path (using relative path since script is in the backend root)
LOCAL_BACKEND_DIR="$(cd "$(dirname "$0")" && pwd)"

# Remote Path on the Server
REMOTE_BACKEND_DIR="/opt/finfra/Middleware/AdvancedJava/OIPA_Security_Tool"

# Backend JAR file details
JAR_NAME="OIPASecurityTool-0.0.1-SNAPSHOT.jar"

# Timestamp suffix for backup naming
DATE_SUFFIX=$(date +%Y%m%d_%H%M%S)

# --- AUTO-DETECT JAVA 11 ---
# If active javac version is older (e.g. Java 8), point to JDK 11 automatically
JAVAC_VER=$(javac -version 2>&1 | head -n 1)
echo "[Local] System javac version: $JAVAC_VER"

if [[ "$JAVAC_VER" == *"1.8"* ]] || [[ "$JAVAC_VER" == *"8.0"* ]] || [[ "$JAVAC_VER" == *"1.8.0"* ]]; then
    echo "[Local] Detected Java 8 active in terminal. Attempting to switch to JDK 11..."
    if [ -d "/c/Program Files/Java/jdk-11" ]; then
        export JAVA_HOME="/c/Program Files/Java/jdk-11"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "[Local] Successfully set JAVA_HOME to $JAVA_HOME"
    elif [ -d "C:/Program Files/Java/jdk-11" ]; then
        export JAVA_HOME="C:/Program Files/Java/jdk-11"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "[Local] Successfully set JAVA_HOME to $JAVA_HOME"
    else
        echo "[Local] Warning: JDK 11 directory not found. Maven build may fail."
    fi
fi

# Print Maven details for troubleshooting
echo "[Local] Maven configuration info:"
mvn -version || true
echo "----------------------------------------------------------------------"

echo "======================================================================"
echo " >>> DEPLOYING BACKEND TO $SERVER_USER@$SERVER_IP"
echo "======================================================================"

# 1. Build backend jar locally
echo "[Local] Navigating to backend directory: $LOCAL_BACKEND_DIR"
cd "$LOCAL_BACKEND_DIR"

echo "[Local] Building backend jar (mvn clean package -DskipTests)..."
mvn clean package -DskipTests

# Verify JAR file exists
if [ ! -f "target/$JAR_NAME" ]; then
    echo "[-] Error: Package jar target/$JAR_NAME does not exist!" >&2
    exit 1
fi
echo "[Local] Jar built successfully: target/$JAR_NAME"

# 2. Rename current jar on server to a backup name with date
echo "[Remote] Checking and backing up existing jar on server..."
ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_IP" \
    "if [ -f \"$REMOTE_BACKEND_DIR/$JAR_NAME\" ]; then \
        mv \"$REMOTE_BACKEND_DIR/$JAR_NAME\" \"$REMOTE_BACKEND_DIR/${JAR_NAME}.bak_${DATE_SUFFIX}\" && \
        echo 'Backup created: ${JAR_NAME}.bak_${DATE_SUFFIX}'; \
     else \
        echo 'No existing jar found to backup at $REMOTE_BACKEND_DIR/$JAR_NAME'; \
     fi"

# 3. Paste the new jar into backend path on server
echo "[Local -> Remote] Copying $JAR_NAME to server..."
scp -P "$SERVER_PORT" "target/$JAR_NAME" "$SERVER_USER@$SERVER_IP:$REMOTE_BACKEND_DIR/"

# Verify the jar was transferred successfully
echo "[Remote] Verifying new JAR exists on server..."
if ! ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_IP" "[ -f \"$REMOTE_BACKEND_DIR/$JAR_NAME\" ]"; then
    echo "[-] Error: Jar was not transferred successfully to $REMOTE_BACKEND_DIR/$JAR_NAME!" >&2
    exit 1
fi
echo "[Remote] JAR verification passed."

# 4. Rollback and run docker scripts
rollback_backend() {
    echo "[Rollback] Restoring backup jar on remote server..."
    ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_IP" \
        "cd \"$REMOTE_BACKEND_DIR\" && \
         if [ -f \"${JAR_NAME}.bak_${DATE_SUFFIX}\" ]; then \
             mv \"${JAR_NAME}.bak_${DATE_SUFFIX}\" \"$JAR_NAME\" && \
             echo 'Restored backup JAR: $JAR_NAME' && \
             echo 'Running restartDocker.sh to restore service...' && \
             ./restartDocker.sh; \
         else \
             echo 'No backup jar found to rollback!'; \
         fi"
}

# Run buildDocker.sh and restartDocker.sh on the server
echo "[Remote] Executing buildDocker.sh and restartDocker.sh..."
if ! ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_IP" \
    "cd \"$REMOTE_BACKEND_DIR\" && \
     chmod +x buildDocker.sh restartDocker.sh && \
     echo 'Running buildDocker.sh...' && ./buildDocker.sh && \
     echo 'Running restartDocker.sh...' && ./restartDocker.sh"; then
    echo "[-] Error: Docker build or restart scripts failed!"
    echo "[-] Initiating rollback..."
    rollback_backend
    exit 1
fi
     
# 5. Verify backend is running on port 8015
echo "[Remote] Verifying backend is active and listening on port 8015..."
if ! ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_IP" '
    success=false
    for i in {1..20}; do
        if curl -s --connect-timeout 2 http://localhost:8015 >/dev/null || ss -tln | grep -q ":8015\b" || netstat -tln | grep -q ":8015 "; then
            success=true
            break
        fi
        echo "Waiting for backend to start (attempt $i/20)..."
        sleep 3
    done
    $success
'; then
    echo "[-] Error: Backend verification failed! Port 8015 is not responding."
    echo "[-] Initiating rollback..."
    rollback_backend
    exit 1
fi

# Clean up old backups if successful (keep only the last 3 for safety)
echo "[Remote] Cleaning up older backups in backend folder..."
ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_IP" \
    "cd \"$REMOTE_BACKEND_DIR\" && ls -t ${JAR_NAME}.bak_* 2>/dev/null | tail -n +4 | xargs rm -f || true"

echo "======================================================================"
echo " >>> BACKEND DEPLOYMENT SUCCESSFUL!"
echo "======================================================================"
