# IReader Distribution Infrastructure

This directory contains configuration and scripts for setting up the distribution infrastructure for IReader, including CDN configuration, update server, and mirror management.

## Components

### 1. CDN Configuration (`cdn_config.yaml`)

Defines the CDN setup for distributing voice models and application updates:
- Primary CDN endpoint
- Mirror endpoints for redundancy
- Rate limiting rules
- Caching strategies
- Geographic distribution

### 2. Update Server (`update_server.py`)

Flask-based API server that handles:
- Version checking
- Update availability
- Release information
- Voice model catalog
- Download coordination

**Endpoints:**
- `GET /api/v1/version/check` - Check for updates
- `GET /api/v1/releases` - List all releases
- `GET /api/v1/release/<version>` - Get release info
- `GET /api/v1/download/<version>/<platform>` - Download release
- `GET /api/v1/voices/catalog` - Get voice catalog
- `GET /api/v1/voices/<voice_id>/download` - Get voice download info
- `GET /health` - Health check

### 3. Nginx Configuration (`nginx_cdn.conf`)

Production-ready Nginx configuration with:
- SSL/TLS termination
- Rate limiting
- Bandwidth throttling
- Caching headers
- CORS support
- Security headers
- Proxy to update server

### 4. Deployment Script (`deploy_cdn.sh`)

Automated deployment script that:
- Sets up directory structure
- Installs dependencies
- Configures Nginx
- Creates systemd services
- Sets up log rotation
- Creates helper scripts

## Quick Start

### Prerequisites

- Linux server (Ubuntu 20.04+ or similar)
- Root access
- Domain name configured (cdn.ireader.org)
- SSL certificates

### Installation

1. **Deploy the CDN infrastructure:**
   ```bash
   sudo ./deploy_cdn.sh
   ```

2. **Configure SSL certificates:**
   ```bash
   # Using Let's Encrypt
   sudo certbot --nginx -d cdn.ireader.org
   ```

3. **Deploy a voice model:**
   ```bash
   sudo /var/www/cdn/deploy_voice.sh en-us-amy-low model.onnx config.json
   ```

4. **Upload application release:**
   ```bash
   # Create release directory
   sudo mkdir -p /var/www/cdn/releases/1.0.0
   
   # Copy release files
   sudo cp IReader-1.0.0-x64.msi /var/www/cdn/releases/1.0.0/
   sudo cp IReader-1.0.0-macOS.dmg /var/www/cdn/releases/1.0.0/
   sudo cp ireader_1.0.0_amd64.deb /var/www/cdn/releases/1.0.0/
   
   # Create release.json
   sudo cat > /var/www/cdn/releases/1.0.0/release.json << 'EOF'
   {
     "version": "1.0.0",
     "release_date": "2025-11-10",
     "channel": "stable",
     "is_critical": false,
     "release_notes": "Initial release with offline TTS support",
     "downloads": {
       "windows": {
         "url": "https://cdn.ireader.org/releases/1.0.0/IReader-1.0.0-x64.msi",
         "filename": "IReader-1.0.0-x64.msi",
         "checksum": "...",
         "size": 52428800
       },
       "macos": {
         "url": "https://cdn.ireader.org/releases/1.0.0/IReader-1.0.0-macOS.dmg",
         "filename": "IReader-1.0.0-macOS.dmg",
         "checksum": "...",
         "size": 62914560
       },
       "linux": {
         "url": "https://cdn.ireader.org/releases/1.0.0/ireader_1.0.0_amd64.deb",
         "filename": "ireader_1.0.0_amd64.deb",
         "checksum": "...",
         "size": 41943040
       }
     }
   }
   EOF
   
   # Set permissions
   sudo chown -R www-data:www-data /var/www/cdn/releases/1.0.0
   ```

## Configuration

### Rate Limiting

Adjust rate limits in `nginx_cdn.conf`:
```nginx
limit_req_zone $binary_remote_addr zone=download_limit:10m rate=10r/m;
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=20r/m;
```

### Bandwidth Throttling

Modify bandwidth limits:
```nginx
limit_rate 10m;  # 10 MB/s per connection
```

### Caching

Adjust cache durations:
```nginx
expires 30d;  # Voice models
expires 7d;   # Releases
expires 1h;   # Catalog
```

## Monitoring

### View Logs

```bash
# Nginx access logs
tail -f /var/log/nginx/cdn_access.log

# Nginx error logs
tail -f /var/log/nginx/cdn_error.log

# Update server logs
journalctl -u ireader-update-server -f
```

### Check Service Status

```bash
# Nginx status
systemctl status nginx

# Update server status
systemctl status ireader-update-server

# Nginx statistics
curl http://localhost:8080/nginx_status
```

### Health Check

```bash
curl https://cdn.ireader.org/health
```

## Mirror Setup

To set up additional mirrors:

1. **Deploy on mirror server:**
   ```bash
   sudo ./deploy_cdn.sh
   ```

2. **Sync content from primary:**
   ```bash
   # Using rsync
   rsync -avz --delete \
     primary.cdn.ireader.org:/var/www/cdn/voices/ \
     /var/www/cdn/voices/
   
   rsync -avz --delete \
     primary.cdn.ireader.org:/var/www/cdn/releases/ \
     /var/www/cdn/releases/
   ```

3. **Set up periodic sync:**
   ```bash
   # Add to crontab
   */30 * * * * rsync -avz --delete primary.cdn.ireader.org:/var/www/cdn/voices/ /var/www/cdn/voices/
   ```

## Security

### SSL/TLS

- Use strong cipher suites
- Enable HSTS
- Obtain certificates from Let's Encrypt or commercial CA

### Access Control

- Implement IP whitelisting for admin endpoints
- Use API keys for authenticated access
- Enable fail2ban for brute force protection

### Content Integrity

- All files include SHA256 checksums
- Verify checksums before serving
- Sign releases with GPG keys

## Troubleshooting

### High CPU Usage

- Check for excessive requests
- Review rate limiting rules
- Enable caching

### Slow Downloads

- Check bandwidth limits
- Verify CDN configuration
- Consider adding more mirrors

### Update Server Not Responding

```bash
# Check logs
journalctl -u ireader-update-server -n 100

# Restart service
sudo systemctl restart ireader-update-server
```

## Maintenance

### Update Voice Catalog

```bash
# Edit catalog
sudo nano /var/www/cdn/voices/catalog.json

# Reload nginx to clear cache
sudo systemctl reload nginx
```

### Clean Old Releases

```bash
# Remove old releases (keep last 3 versions)
cd /var/www/cdn/releases
ls -t | tail -n +4 | xargs rm -rf
```

### Backup

```bash
# Backup voice models and releases
tar -czf cdn-backup-$(date +%Y%m%d).tar.gz /var/www/cdn/
```

## Performance Optimization

### Enable HTTP/2

Already enabled in nginx configuration.

### Enable Gzip Compression

Add to nginx configuration:
```nginx
gzip on;
gzip_types application/json text/plain;
gzip_min_length 1000;
```

### Use CDN Service

Consider using Cloudflare, AWS CloudFront, or similar for global distribution.

## Support

For issues or questions:
- GitHub Issues: https://github.com/yourusername/ireader/issues
- Email: support@ireader.org
