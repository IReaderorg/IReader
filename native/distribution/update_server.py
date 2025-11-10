#!/usr/bin/env python3
"""
Update Server for IReader
Handles version checks and update distribution
"""

import json
import hashlib
import os
from datetime import datetime
from typing import Dict, List, Optional
from pathlib import Path

from flask import Flask, jsonify, request, send_file, abort
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from flask_cors import CORS

app = Flask(__name__)
CORS(app, origins=["https://ireader.org", "https://*.ireader.org"])

# Rate limiting
limiter = Limiter(
    app=app,
    key_func=get_remote_address,
    default_limits=["100 per hour", "10 per minute"]
)

# Configuration
RELEASES_DIR = Path(os.getenv("RELEASES_DIR", "./releases"))
CATALOG_FILE = Path(os.getenv("CATALOG_FILE", "./catalog.json"))
CURRENT_VERSION = "1.0.0"


class UpdateServer:
    """Manages software updates and version information"""
    
    def __init__(self, releases_dir: Path):
        self.releases_dir = releases_dir
        self.releases_cache = {}
        self._load_releases()
    
    def _load_releases(self):
        """Load all available releases"""
        if not self.releases_dir.exists():
            return
        
        for release_file in self.releases_dir.glob("*/release.json"):
            with open(release_file) as f:
                release_info = json.load(f)
                version = release_info["version"]
                self.releases_cache[version] = release_info
    
    def get_latest_version(self, platform: str, channel: str = "stable") -> Optional[Dict]:
        """Get the latest version for a platform and channel"""
        matching_releases = [
            r for r in self.releases_cache.values()
            if r.get("platform") == platform and r.get("channel") == channel
        ]
        
        if not matching_releases:
            return None
        
        # Sort by version (semantic versioning)
        sorted_releases = sorted(
            matching_releases,
            key=lambda r: tuple(map(int, r["version"].split("."))),
            reverse=True
        )
        
        return sorted_releases[0]
    
    def get_release_info(self, version: str) -> Optional[Dict]:
        """Get information about a specific release"""
        return self.releases_cache.get(version)
    
    def get_download_url(self, version: str, platform: str) -> Optional[str]:
        """Get download URL for a specific version and platform"""
        release = self.get_release_info(version)
        if not release:
            return None
        
        downloads = release.get("downloads", {})
        return downloads.get(platform, {}).get("url")


update_server = UpdateServer(RELEASES_DIR)


@app.route("/api/v1/version/check", methods=["GET"])
@limiter.limit("20 per minute")
def check_version():
    """Check for available updates"""
    current_version = request.args.get("version", "0.0.0")
    platform = request.args.get("platform", "unknown")
    channel = request.args.get("channel", "stable")
    
    latest = update_server.get_latest_version(platform, channel)
    
    if not latest:
        return jsonify({
            "update_available": False,
            "message": "No updates available"
        })
    
    latest_version = latest["version"]
    update_available = _is_newer_version(latest_version, current_version)
    
    response = {
        "update_available": update_available,
        "current_version": current_version,
        "latest_version": latest_version,
    }
    
    if update_available:
        response.update({
            "download_url": latest["downloads"][platform]["url"],
            "release_notes": latest.get("release_notes", ""),
            "release_date": latest.get("release_date", ""),
            "is_critical": latest.get("is_critical", False),
            "checksum": latest["downloads"][platform]["checksum"],
            "size_bytes": latest["downloads"][platform]["size"]
        })
    
    return jsonify(response)


@app.route("/api/v1/releases", methods=["GET"])
@limiter.limit("30 per minute")
def list_releases():
    """List all available releases"""
    platform = request.args.get("platform")
    channel = request.args.get("channel")
    
    releases = list(update_server.releases_cache.values())
    
    # Filter by platform if specified
    if platform:
        releases = [r for r in releases if r.get("platform") == platform]
    
    # Filter by channel if specified
    if channel:
        releases = [r for r in releases if r.get("channel") == channel]
    
    # Sort by version (newest first)
    releases.sort(
        key=lambda r: tuple(map(int, r["version"].split("."))),
        reverse=True
    )
    
    return jsonify({
        "releases": releases,
        "count": len(releases)
    })


@app.route("/api/v1/release/<version>", methods=["GET"])
@limiter.limit("30 per minute")
def get_release(version: str):
    """Get information about a specific release"""
    release = update_server.get_release_info(version)
    
    if not release:
        abort(404, description="Release not found")
    
    return jsonify(release)


@app.route("/api/v1/download/<version>/<platform>", methods=["GET"])
@limiter.limit("5 per minute")
def download_release(version: str, platform: str):
    """Download a specific release"""
    release = update_server.get_release_info(version)
    
    if not release:
        abort(404, description="Release not found")
    
    downloads = release.get("downloads", {})
    if platform not in downloads:
        abort(404, description=f"Platform {platform} not available for this release")
    
    file_path = RELEASES_DIR / version / downloads[platform]["filename"]
    
    if not file_path.exists():
        abort(404, description="Release file not found")
    
    return send_file(
        file_path,
        as_attachment=True,
        download_name=downloads[platform]["filename"]
    )


@app.route("/api/v1/voices/catalog", methods=["GET"])
@limiter.limit("60 per minute")
def get_voice_catalog():
    """Get the voice model catalog"""
    if not CATALOG_FILE.exists():
        abort(404, description="Catalog not found")
    
    with open(CATALOG_FILE) as f:
        catalog = json.load(f)
    
    return jsonify(catalog)


@app.route("/api/v1/voices/<voice_id>/download", methods=["GET"])
@limiter.limit("10 per minute")
def download_voice(voice_id: str):
    """Get download information for a voice model"""
    with open(CATALOG_FILE) as f:
        catalog = json.load(f)
    
    voice = next((v for v in catalog["voices"] if v["id"] == voice_id), None)
    
    if not voice:
        abort(404, description="Voice not found")
    
    return jsonify({
        "voice_id": voice_id,
        "download_url": voice["downloadUrl"],
        "config_url": voice["configUrl"],
        "checksum": voice["checksum"],
        "size": voice["modelSize"]
    })


@app.route("/health", methods=["GET"])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "timestamp": datetime.utcnow().isoformat(),
        "version": CURRENT_VERSION
    })


def _is_newer_version(latest: str, current: str) -> bool:
    """Compare version strings (semantic versioning)"""
    latest_parts = tuple(map(int, latest.split(".")))
    current_parts = tuple(map(int, current.split(".")))
    return latest_parts > current_parts


def _calculate_checksum(file_path: Path) -> str:
    """Calculate SHA256 checksum of a file"""
    sha256 = hashlib.sha256()
    with open(file_path, "rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            sha256.update(chunk)
    return sha256.hexdigest()


if __name__ == "__main__":
    # Development server
    app.run(host="0.0.0.0", port=5000, debug=False)
