#!/bin/bash
# Deploy CDN infrastructure for IReader distribution

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CDN_ROOT="${CDN_ROOT:-/var/www/cdn}"
NGINX_CONF="${NGINX_CONF:-/etc/nginx/sites-available/ireader-cdn}"

echo "Deploying IReader CDN infrastructure..."
echo "======================================="

# Check for root privileges
if [ "$EUID" -ne 0 ]; then
    echo "Error: This script must be run as root"
    exit 1
fi

# Check for required tools
for cmd in nginx python3 pip3; do
    if ! command -v $cmd &> /dev/null; then
        echo "Error: $cmd is not installed"
        exit 1
    fi
done

# Create directory structure
echo "Creating directory structure..."
mkdir -p "$CDN_ROOT"/{voices,releases,errors}
mkdir -p /var/log/nginx

# Set permissions
chown -R www-data:www-data "$CDN_ROOT"
chmod -R 755 "$CDN_ROOT"

# Install Python dependencies for update server
echo "Installing Python dependencies..."
pip3 install flask flask-limiter flask-cors gunicorn

# Copy nginx configuration
echo "Installing nginx configuration..."
cp "$SCRIPT_DIR/nginx_cdn.conf" "$NGINX_CONF"

# Enable site
ln -sf "$NGINX_CONF" /etc/nginx/sites-enabled/ireader-cdn

# Test nginx configuration
echo "Testing nginx configuration..."
nginx -t

# Create systemd service for update server
echo "Creating systemd service for update server..."
cat > /etc/systemd/system/ireader-update-server.service << EOF
[Unit]
Description=IReader Update Server
After=network.target

[Service]
Type=notify
User=www-data
Group=www-data
WorkingDirectory=$SCRIPT_DIR
Environment="RELEASES_DIR=$CDN_ROOT/releases"
Environment="CATALOG_FILE=$CDN_ROOT/voices/catalog.json"
ExecStart=/usr/bin/gunicorn --bind 127.0.0.1:5000 --workers 4 --timeout 60 update_server:app
ExecReload=/bin/kill -s HUP \$MAINPID
KillMode=mixed
TimeoutStopSec=5
PrivateTmp=true

[Install]
WantedBy=multi-user.target
EOF

# Reload systemd
systemctl daemon-reload

# Enable and start services
echo "Starting services..."
systemctl enable ireader-update-server
systemctl start ireader-update-server

# Reload nginx
systemctl reload nginx

# Create initial catalog file
if [ ! -f "$CDN_ROOT/voices/catalog.json" ]; then
    echo "Creating initial voice catalog..."
    cat > "$CDN_ROOT/voices/catalog.json" << 'EOF'
{
  "version": "1.0.0",
  "updated": "2025-11-10T00:00:00Z",
  "voices": []
}
EOF
    chown www-data:www-data "$CDN_ROOT/voices/catalog.json"
fi

# Create error pages
echo "Creating error pages..."
cat > "$CDN_ROOT/errors/404.html" << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>404 Not Found</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
        h1 { color: #333; }
    </style>
</head>
<body>
    <h1>404 - File Not Found</h1>
    <p>The requested file could not be found on this server.</p>
</body>
</html>
EOF

cat > "$CDN_ROOT/errors/50x.html" << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Server Error</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
        h1 { color: #333; }
    </style>
</head>
<body>
    <h1>Server Error</h1>
    <p>An error occurred while processing your request. Please try again later.</p>
</body>
</html>
EOF

# Set up log rotation
echo "Configuring log rotation..."
cat > /etc/logrotate.d/ireader-cdn << 'EOF'
/var/log/nginx/cdn_*.log {
    daily
    missingok
    rotate 14
    compress
    delaycompress
    notifempty
    create 0640 www-data adm
    sharedscripts
    postrotate
        [ -f /var/run/nginx.pid ] && kill -USR1 `cat /var/run/nginx.pid`
    endscript
}
EOF

# Create deployment script for voice models
cat > "$CDN_ROOT/deploy_voice.sh" << 'EOF'
#!/bin/bash
# Deploy a voice model to the CDN

set -e

if [ $# -lt 2 ]; then
    echo "Usage: $0 <voice_id> <model_file> [config_file]"
    exit 1
fi

VOICE_ID="$1"
MODEL_FILE="$2"
CONFIG_FILE="${3:-}"

VOICE_DIR="/var/www/cdn/voices/$VOICE_ID"

# Create voice directory
mkdir -p "$VOICE_DIR"

# Copy model file
cp "$MODEL_FILE" "$VOICE_DIR/"
MODEL_FILENAME=$(basename "$MODEL_FILE")

# Copy config file if provided
if [ -n "$CONFIG_FILE" ]; then
    cp "$CONFIG_FILE" "$VOICE_DIR/"
fi

# Calculate checksum
CHECKSUM=$(sha256sum "$VOICE_DIR/$MODEL_FILENAME" | awk '{print $1}')
echo "$CHECKSUM" > "$VOICE_DIR/$MODEL_FILENAME.sha256"

# Set permissions
chown -R www-data:www-data "$VOICE_DIR"
chmod -R 755 "$VOICE_DIR"

echo "Voice model deployed successfully!"
echo "Voice ID: $VOICE_ID"
echo "Model: $MODEL_FILENAME"
echo "Checksum: $CHECKSUM"
echo "URL: https://cdn.ireader.org/voices/$VOICE_ID/$MODEL_FILENAME"
EOF

chmod +x "$CDN_ROOT/deploy_voice.sh"

# Print status
echo ""
echo "CDN deployment complete!"
echo "========================"
echo "CDN Root: $CDN_ROOT"
echo "Nginx Config: $NGINX_CONF"
echo ""
echo "Services:"
echo "  - Nginx: $(systemctl is-active nginx)"
echo "  - Update Server: $(systemctl is-active ireader-update-server)"
echo ""
echo "Endpoints:"
echo "  - Voice Models: https://cdn.ireader.org/voices/"
echo "  - Releases: https://cdn.ireader.org/releases/"
echo "  - API: https://cdn.ireader.org/api/"
echo "  - Health: https://cdn.ireader.org/health"
echo ""
echo "To deploy a voice model:"
echo "  $CDN_ROOT/deploy_voice.sh <voice_id> <model_file> [config_file]"
echo ""
echo "To view logs:"
echo "  tail -f /var/log/nginx/cdn_access.log"
echo "  journalctl -u ireader-update-server -f"
