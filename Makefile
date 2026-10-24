.DEFAULT_GOAL := help

# Prefer Docker when available, fall back to Podman.
ifeq ($(shell command -v docker 2>/dev/null),)
  CONTAINER_RUNTIME := podman
  COMPOSE := podman compose
else
  CONTAINER_RUNTIME := docker
  COMPOSE := docker compose
endif

.PHONY: help up down run seed wait reset quadlet-install quadlet-uninstall quadlet-start quadlet-stop quadlet-status enable-linger backup backup-postgres backup-redis restore-postgres restore-redis

help: ## Show available targets
	@echo "Usage: make <target>"
	@echo "Container runtime: $(CONTAINER_RUNTIME)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'

# ---- Docker Compose (dev) ----

up: ## Start services (docker/podman compose)
	$(COMPOSE) up -d

down: ## Stop and remove containers and volumes (docker/podman compose)
	$(COMPOSE) down -v

run: ## Build jar and run it
	./gradlew bootJar
	java -jar build/libs/simonjp-0.0.1-SNAPSHOT.jar

seed: ## Run gradle seed task
	./gradlew seed

wait: ## Wait for postgres and valkey readiness
	@until $(COMPOSE) exec -T postgres pg_isready -U admin -d simonjp >/dev/null 2>&1; do sleep 1; done
	@until $(COMPOSE) exec -T valkey valkey-cli -a admin ping 2>/dev/null | grep -q PONG; do sleep 1; done

reset: down up wait seed ## Reset DB: down, up, wait, seed

# ---- Podman / Quadlet (deploy) ----

QUADLET_DIR ?= $(HOME)/.config/containers/systemd
BACKUP_DIR ?= ./backups
TIMESTAMP := $(shell date +%Y%m%d-%H%M%S)

# Auto-detect running simonjp postgres/valkey containers (dev compose or quadlet).
PG_CONTAINER ?= $(shell $(CONTAINER_RUNTIME) ps --format '{{.Names}}' 2>/dev/null | grep -iE 'simonjp.*postgres|postgres.*simonjp' | head -1)
REDIS_CONTAINER ?= $(shell $(CONTAINER_RUNTIME) ps --format '{{.Names}}' 2>/dev/null | grep -iE 'simonjp.*(valkey|redis)|(valkey|redis).*simonjp' | grep -iv 'commander' | head -1)

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
	@test -n "$(PG_CONTAINER)" || (echo "No running simonjp postgres container found. Start it first."; exit 1)
	@mkdir -p $(BACKUP_DIR)
	$(CONTAINER_RUNTIME) exec $(PG_CONTAINER) pg_dump -U admin -d simonjp --clean --if-exists --no-owner --no-acl > $(BACKUP_DIR)/postgres-$(TIMESTAMP).sql
	@echo "Backup written to $(BACKUP_DIR)/postgres-$(TIMESTAMP).sql (container: $(PG_CONTAINER))"

backup-redis: ## Backup Valkey to backups/redis-<timestamp>.rdb
	@test -n "$(REDIS_CONTAINER)" || (echo "No running simonjp valkey/redis container found. Start it first."; exit 1)
	@mkdir -p $(BACKUP_DIR)
	$(CONTAINER_RUNTIME) exec $(REDIS_CONTAINER) valkey-cli -a admin BGSAVE
	@sleep 1
	$(CONTAINER_RUNTIME) cp $(REDIS_CONTAINER):/data/dump.rdb $(BACKUP_DIR)/redis-$(TIMESTAMP).rdb
	@echo "Backup written to $(BACKUP_DIR)/redis-$(TIMESTAMP).rdb (container: $(REDIS_CONTAINER))"

backup: backup-postgres backup-redis ## Backup both PostgreSQL and Valkey

restore-postgres: ## Restore PostgreSQL from FILE=...
	@test -n "$(FILE)" || (echo "Usage: make restore-postgres FILE=backups/postgres-xxx.sql"; exit 1)
	@test -n "$(PG_CONTAINER)" || (echo "No running simonjp postgres container found. Start it first."; exit 1)
	$(CONTAINER_RUNTIME) exec -i $(PG_CONTAINER) psql -U admin -d simonjp < $(FILE)
	@echo "Restored from $(FILE) (container: $(PG_CONTAINER))"

restore-redis: ## Restore Valkey from FILE=...
	@test -n "$(FILE)" || (echo "Usage: make restore-redis FILE=backups/redis-xxx.rdb"; exit 1)
	@test -n "$(REDIS_CONTAINER)" || (echo "No running simonjp valkey/redis container found. Start it first."; exit 1)
	$(CONTAINER_RUNTIME) stop $(REDIS_CONTAINER)
	$(CONTAINER_RUNTIME) cp $(FILE) $(REDIS_CONTAINER):/data/dump.rdb
	$(CONTAINER_RUNTIME) start $(REDIS_CONTAINER)
	@echo "Restored from $(FILE) (container: $(REDIS_CONTAINER))"
