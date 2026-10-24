.DEFAULT_GOAL := help

.PHONY: help up down start seed reset

help: ## Show available targets
	@echo "Usage: make <target>"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-10s %s\n", $$1, $$2}'

up: ## Start services
	docker compose up -d

down: ## Stop and remove containers and volumes
	docker compose down -v

start: ## Build jar and run it
	./gradlew bootJar
	java -jar build/libs/simonjp-0.0.1-SNAPSHOT.jar

seed: ## Run gradle seed task
	./gradlew seed

reset: ## Reset DB: down, up, seed
	$(MAKE) down
	$(MAKE) up
	@until docker compose exec -T postgres pg_isready -U admin -d simonjp >/dev/null 2>&1; do sleep 1; done
	@until docker compose exec -T valkey valkey-cli -a admin ping 2>/dev/null | grep -q PONG; do sleep 1; done
	$(MAKE) seed