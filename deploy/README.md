# Deploy with Podman Quadlet

Quadlet turns the `.container`/`.network`/`.volume` files in `quadlet/` into
systemd units. Install them into your user systemd directory:

```sh
make quadlet-install
make enable-linger   # optional: run services without an active login
```

The app image is pulled from GHCR. Start the services:

```sh
make quadlet-start
make quadlet-status
```

The app is then available at http://127.0.0.1:8080, Adminer at
http://127.0.0.1:8083, Redis Commander at http://127.0.0.1:8084.

For a system-wide (root) install instead, override the target directory:

```sh
sudo make quadlet-install QUADLET_DIR=/etc/containers/systemd
```

and use `systemctl` (without `--user`).

## Backup / Restore

```sh
make backup                       # postgres + valkey
make backup-postgres
make backup-redis

make restore-postgres FILE=backups/postgres-YYYYMMDD-HHMMSS.sql
make restore-redis FILE=backups/redis-YYYYMMDD-HHMMSS.rdb
```

Backups are written to `./backups/` (gitignored).
