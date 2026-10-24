.DEFAULT_GOAL := help

.PHONY: help up down run seed wait reset podman-build quadlet-install quadlet-uninstall quadlet-start quadlet-stop quadlet-status enable-linger backup backup-postgres backup-redis restore-postgres restore-redis

help: ## Show available targets
	@echo "Usage: make <target>"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'

# ---- Docker Compose (dev) ----

up: ## Start services (docker compose)
	docker compose up -d

down: ## Stop and remove containers and volumes (docker compose)
	docker compose down -v

run: ## Build jar and run it
	./gradlew bootJar
	java -jar build/libs/simonjp-0.0.1-SNAPSHOT.jar

seed: ## Run gradle seed task
	./gradlew seed

wait: ## Wait for postgres and valkey readiness
	@until docker compose exec -T postgres pg_isready -U admin -d simonjp >/dev/null 2>&1; do sleep 1; done
	@until docker compose exec -T valkey valkey-cli -a admin ping 2>/dev/null | grep -q PONG; do sleep 1; done

reset: down up wait seed ## Reset DB: down, up, wait, seed

# ---- Podman / Quadlet (deploy) ----

QUADLET_DIR ?= $(HOME)/.config/containers/systemd
BACKUP_DIR ?= ./backups
TIMESTAMP := $(shell date +%Y%m%d-%H%M%S)

podman-build: ## Build native binary and container image
	./gradlew nativeCompile
	podman build -t simonjp:latest .

quadlet-install: ## Install quadlet files into $(QUADLET_DIR)
	@mkdir -p $(QUADLET_DIR)
	cp deploy/quadlet/*.container deploy/quadlet/*.network deploy/quadlet/*.volume $(QUADLET_DIR)/
	systemctl --user daemon-reload
	@echo "Installed. Start with: make quadlet-start"

quadlet-uninstall: ## Remove quadlet files from $(QUADLET_DIR)
	rm -f $(QUADLET_DIR)/simonjp.container $(QUADLET_DIR)/simonjp-*.container $(QUADLET_DIR)/simonjp.network $(QUADLET_DIR)/simonjp-*.volume
	systemctl --user daemon-reload

quadlet-start: ## Start quadlet services
	systemctl --user start simonjp.service

quadlet-stop: ## Stop quadlet services
	systemctl --user stop simonjp.service simonjp-adminer.service simonjp-redis-commander.service

quadlet-status: ## Show quadlet service status
	systemctl --user status simonjp.service simonjp-postgres.service simonjp-valkey.service --no-pager

enable-linger: ## Enable lingering so user services run without login
	loginctl enable-linger $(USER)

# ---- Backup / Restore ----

backup-postgres: ## Backup PostgreSQL to backups/postgres-<timestamp>.sql
	@mkdir -p $(BACKUP_DIR)
	podman exec simonjp-postgres pg_dump -U admin -d simonjp --clean --if-exists --no-owner --no-acl > $(BACKUP_DIR)/postgres-$(TIMESTAMP).sql
	@echo "Backup written to $(BACKUP_DIR)/postgres-$(TIMESTAMP).sql"

backup-redis: ## Backup Valkey to backups/redis-<timestamp>.rdb
	@mkdir -p $(BACKUP_DIR)
	podman exec simonjp-valkey valkey-cli -a admin BGSAVE
	@sleep 1
	podman cp simonjp-valkey:/data/dump.rdb $(BACKUP_DIR)/redis-$(TIMESTAMP).rdb
	@echo "Backup written to $(BACKUP_DIR)/redis-$(TIMESTAMP).rdb"

backup: backup-postgres backup-redis ## Backup both PostgreSQL and Valkey

restore-postgres: ## Restore PostgreSQL from FILE=...
	@test -n "$(FILE)" || (echo "Usage: make restore-postgres FILE=backups/postgres-xxx.sql"; exit 1)
	podman exec -i simonjp-postgres psql -U admin -d simonjp < $(FILE)
	@echo "Restored from $(FILE)"

restore-redis: ## Restore Valkey from FILE=...
	@test -n "$(FILE)" || (echo "Usage: make restore-redis FILE=backups/redis-xxx.rdb"; exit 1)
	podman stop simonjp-valkey
	podman run --rm -v simonjp-valkey-data:/data -v $(abspath $(FILE)):/backup.rdb:ro,Z busybox sh -c 'cp /backup.rdb /data/dump.rdb'
	podman start simonjp-valkey
	@echo "Restored from $(FILE)"
